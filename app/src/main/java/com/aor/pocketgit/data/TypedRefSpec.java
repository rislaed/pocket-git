package com.aor.pocketgit.data;

import org.eclipse.jgit.transport.RefSpec;

public class TypedRefSpec {
    private RefSpec mRefSpec;
    private TYPE mType;

    public enum TYPE {
        FETCH,
        PUSH
    }

    public TypedRefSpec(RefSpec refSpec, TYPE type) {
        this.mRefSpec = refSpec;
        this.mType = type;
    }

    public RefSpec getRefSpec() {
        return this.mRefSpec;
    }

    public TYPE getType() {
        return this.mType;
    }
}
