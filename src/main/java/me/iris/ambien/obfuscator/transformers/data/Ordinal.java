package me.iris.ambien.obfuscator.transformers.data;

/**
 * Order for applying transformers
 */
public enum Ordinal {
    /**
     * Transformer will be applied last
     */
    LOW (0),


    /**
     * Transformer will be applied in the middle (whenever)
     */
    STANDARD(1),


    /**
     * Transformer will be applied first
     */
    HIGH(2);

    private final int idx;
    Ordinal(final int idx) {
        this.idx = idx;
    }

    /**
     * @return Ordinal as int
     */
    public int getIdx() {
        return idx;
    }
}
