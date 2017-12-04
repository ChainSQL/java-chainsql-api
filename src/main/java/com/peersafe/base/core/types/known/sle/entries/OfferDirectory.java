package com.peersafe.base.core.types.known.sle.entries;

import com.peersafe.base.core.coretypes.Issue;
import com.peersafe.base.core.coretypes.IssuePair;

public class OfferDirectory extends DirectoryNode {
    public IssuePair issuePair() {
        return new IssuePair(takerPaysIssue(), takerGetsIssue());
    }

    public Issue takerGetsIssue() {
        // TODO: remove wrapper
        return Issue.from160s(takerGetsCurrency(), takerGetsIssuer());
    }
    public Issue takerPaysIssue() {
        // TODO: remove wrapper
        return Issue.from160s(takerPaysCurrency(), takerPaysIssuer());
    }

}
