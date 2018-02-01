/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package co.rsk.core.types;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

/**
 * A BlockHash just wraps a byte[] so that equals and hashcode work correctly,
 * allowing it to be used as keys in a map.
 */
public class BlockHash implements Comparable<BlockHash> {
    private final byte[] bytes;
    private static final BlockHash ZERO_HASH = new BlockHash(new byte[32]);
    private static final BlockHash EMPTY_LIST_HASH = new BlockHash(HashUtil.sha3(RLP.encodeList()));


    public static BlockHash zeroHash() {
        return ZERO_HASH;
    }

    public static BlockHash emptyListHash() {
        return EMPTY_LIST_HASH;
    }

    public BlockHash(byte[] rawHashBytes) {
        this.bytes = rawHashBytes;
    }

    public BlockHash(String hexString) {
        if (hexString == null) {
            this.bytes = EMPTY_BYTE_ARRAY;
            return;
        }
        if (hexString.startsWith("0x")) {
            hexString = hexString.substring(2);
        }
        this.bytes = Hex.decode(hexString);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass() && Arrays.equals(bytes, ((BlockHash) o).bytes);
    }

    /**
     * Returns the last four bytes of the wrapped hash. This should be unique enough to be a suitable hash code even for
     * blocks, where the goal is to try and get the first bytes to be zeros (i.e. the value as a big integer lower
     * than the target value).
     */
    @Override
    public int hashCode() {
       return Arrays.hashCode(this.bytes);
    }

    @Override
    public String toString() {
        return bytes == null ? "" : Hex.toHexString(bytes);
    }

    public String toJsonString() {
        return "0x"+this.toString();
    }
    /**
     * Returns the internal byte array, without defensively copying. Therefore do NOT modify the returned array.
     */
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public int compareTo(BlockHash o) {
        if (bytes == o.bytes && bytes.length == o.bytes.length) {
            return 0;
        }
        for (int i = 0, j = 0; i < bytes.length && j < o.bytes.length; i++, j++) {
            int a = (bytes[i] & 0xff);
            int b = (o.bytes[j] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return bytes.length - o.bytes.length;
    }
}
