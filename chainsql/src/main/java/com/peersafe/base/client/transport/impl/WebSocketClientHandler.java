/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
//The MIT License
//
//Copyright (c) 2009 Carl Bystršm
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.

package com.peersafe.base.client.transport.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;
import io.netty.bootstrap.Bootstrap;

import java.lang.ref.WeakReference;
import com.peersafe.base.client.transport.TransportEventHandler;
import org.json.JSONObject;
import java.net.URI;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    WeakReference<TransportEventHandler> tranEventh;
    private Channel channel_;
    String appendframeData_ = "";

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    /**
     * setEventHandler
     * @param eventHandler eventHandler
     */
    public void setEventHandler(TransportEventHandler eventHandler) {
        tranEventh = new WeakReference<TransportEventHandler>(eventHandler);
    }

    public void doConnect(Bootstrap bs, URI uri) {
        ChannelFuture chf = bs.connect(uri.getHost(), uri.getPort());
        chf.addListener(new ChannelFutureListener() {
            @Override public void operationComplete(ChannelFuture future)
                throws Exception {
                TransportEventHandler teHandler = tranEventh.get();
                if( !future.isSuccess() ) {
                    future.channel().close();
                    // bs.connect(uri.getHost(), uri.getPort()).addListener(this);
                    if (teHandler != null) {
                        teHandler.onError((Exception)future.cause());
                        teHandler.onDisconnected(false);
                    }
                } else {
                    channel_ = future.channel();
                    //add a listener to detect the connection lost
                    addCloseDetectListener(future.channel());
                }
            }

            private void addCloseDetectListener(Channel channel) {
                channel.closeFuture().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future )
                    throws Exception {
                        TransportEventHandler teHandler = tranEventh.get();
                        if (teHandler != null) {
                            teHandler.onDisconnected(false);
                        }
                    }
                });
            }
        });
    }
    public void sendMessage(String msg) {
        WebSocketFrame frame = new TextWebSocketFrame(msg);
        channel_.writeAndFlush(frame);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            TransportEventHandler teHandler = tranEventh.get();
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                System.out.println("WebSocket Client connected!");
                if (teHandler != null) {
                    teHandler.onConnected();
                }
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                System.out.println("WebSocket Client failed to connect");
                if (teHandler != null) {
                    teHandler.onDisconnected(false);
                }
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            // System.out.println("WebSocket Client received message: " + textFrame.text());
            String frameStr = textFrame.text();
            if(textFrame.isFinalFragment()){
                TransportEventHandler teHandler = tranEventh.get();
                if (teHandler != null) {
                    try {
                        teHandler.onMessage(new JSONObject(frameStr));
                    } catch (Exception e) {
                       e.printStackTrace();
                    }
                }
            } else {
                appendframeData_ += frameStr;
            }
        } else if (frame instanceof ContinuationWebSocketFrame) {
            String nextFrameStr = ((ContinuationWebSocketFrame) frame).text();
            appendframeData_ += nextFrameStr;
            if(frame.isFinalFragment()) {
                TransportEventHandler teHandler = tranEventh.get();
                if (teHandler != null) {
                    // System.out.println("****Pingjie String Ret****: " + appendframeData_);
                    try {
                        teHandler.onMessage(new JSONObject(appendframeData_));
                        appendframeData_ = "";
                    } catch (Exception e) {
                       e.printStackTrace();
                    }
                }
            }
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        TransportEventHandler teHandler = tranEventh.get();
        if (teHandler != null) {
            teHandler.onError((Exception)cause);
        }
        ctx.close();
    }
}
