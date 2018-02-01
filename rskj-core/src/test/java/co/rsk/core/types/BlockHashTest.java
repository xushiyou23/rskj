package co.rsk.core.types;

import org.ethereum.rpc.TypeConverter;
import org.junit.Assert;
import org.junit.Test;

public class BlockHashTest {

    @Test
    public void toJsonStringTest() {
        String expected = TypeConverter.toJsonHex(BlockHash.zeroHash().getBytes());
        Assert.assertEquals(expected, BlockHash.zeroHash().toJsonString());
    }

    @Test
    public void compareToTest() {
        BlockHash lowHash = new BlockHash(new byte[31]);
        BlockHash highHash = new BlockHash(new byte[32]);
        byte[] rawHighHash = new byte[32];
        rawHighHash[0] = 0x01;
        BlockHash anotherHighHash = new BlockHash(rawHighHash);

        Assert.assertEquals(lowHash.compareTo(highHash), -1);
        Assert.assertEquals(highHash.compareTo(highHash), 0);
        Assert.assertEquals(highHash.compareTo(lowHash), 1);

        Assert.assertEquals(anotherHighHash.compareTo(highHash), 1);
        Assert.assertEquals(highHash.compareTo(anotherHighHash), -1);
    }
}
