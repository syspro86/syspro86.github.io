public class AnsiX923Padding {
    private static final byte PADDING_VALUE = 0x00;

    public byte[] addPadding(byte[] source, int blockSize) {
        int paddingCnt = blockSize - (source.length % blockSize);
        byte[] padded = new byte[source.length + paddingCnt];

        System.arraycopy(source, 0, padded, 0, source.length);
        Arrays.fill(padded, source.length, padded.length - 1, PADDING_VALUE);
        padded[padded.length - 1] = (byte) paddingCnt;

        return padded;
    }

    public byte[] removePadding(byte[] source, int blockSize) {
        int paddingCnt = source[source.length - 1];
        
        byte[] unpadded = new byte[source.length - paddingCnt];
        System.arraycopy(source, 0, unpadded, 0, unpadded.length);

        return unpadded;
    }
}
