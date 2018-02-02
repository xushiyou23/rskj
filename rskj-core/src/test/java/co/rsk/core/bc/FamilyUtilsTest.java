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

package co.rsk.core.bc;

import co.rsk.blockchain.utils.BlockGenerator;
import co.rsk.config.RskSystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.db.BlockStore;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.IndexedBlockStore;
import org.junit.Assert;
import org.junit.Test;
import org.mapdb.DB;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by ajlopez on 12/08/2016.
 */
public class FamilyUtilsTest {
    @Test
    public void getFamilyGetParent() {
        BlockStore store = createBlockStore();

        BlockGenerator blockGenerator = new BlockGenerator();
        Block genesis = blockGenerator.getGenesisBlock();
        Block block1 = blockGenerator.createChildBlock(genesis);

        store.saveBlock(genesis, BigInteger.ONE, true);
        store.saveBlock(block1, BigInteger.ONE, true);

        Set<ByteArrayWrapper> family = FamilyUtils.getFamily(store, block1, 6);

        Assert.assertNotNull(family);
        Assert.assertFalse(family.isEmpty());
        Assert.assertEquals(1, family.size());

        Assert.assertTrue(family.contains(genesis.getWrappedHash()));
    }

    @Test
    public void getEmptyFamilyForGenesis() {
        BlockStore store = createBlockStore();

        Block genesis = new BlockGenerator().getGenesisBlock();

        store.saveBlock(genesis, BigInteger.ONE, true);

        Set<ByteArrayWrapper> family = FamilyUtils.getFamily(store, genesis, 6);

        Assert.assertNotNull(family);
        Assert.assertTrue(family.isEmpty());
    }

    @Test
    public void getFamilyGetAncestorsUpToLevel() {
        BlockStore store = createBlockStore();

        BlockGenerator blockGenerator = new BlockGenerator();
        Block genesis = blockGenerator.getGenesisBlock();
        Block block1 = blockGenerator.createChildBlock(genesis);
        Block block2 = blockGenerator.createChildBlock(block1);
        Block block3 = blockGenerator.createChildBlock(block2);

        store.saveBlock(genesis, BigInteger.ONE, true);
        store.saveBlock(block1, BigInteger.ONE, true);
        store.saveBlock(block2, BigInteger.ONE, true);
        store.saveBlock(block3, BigInteger.ONE, true);

        Set<ByteArrayWrapper> family = FamilyUtils.getFamily(store, block3, 2);

        Assert.assertNotNull(family);
        Assert.assertFalse(family.isEmpty());
        Assert.assertEquals(2, family.size());

        Assert.assertFalse(family.contains(genesis.getWrappedHash()));
        Assert.assertFalse(family.contains(block3.getWrappedHash()));
        Assert.assertTrue(family.contains(block1.getWrappedHash()));
        Assert.assertTrue(family.contains(block2.getWrappedHash()));
    }

    @Test
    public void getFamilyGetAncestorsWithUncles() {
        BlockStore store = createBlockStore();

        BlockGenerator blockGenerator = new BlockGenerator();
        Block genesis = blockGenerator.getGenesisBlock();
        Block block1 = blockGenerator.createChildBlock(genesis);
        Block uncle11 = blockGenerator.createChildBlock(genesis);
        Block uncle12 = blockGenerator.createChildBlock(genesis);
        Block block2 = blockGenerator.createChildBlock(block1);
        Block uncle21 = blockGenerator.createChildBlock(block1);
        Block uncle22 = blockGenerator.createChildBlock(block1);
        Block block3 = blockGenerator.createChildBlock(block2);
        Block uncle31 = blockGenerator.createChildBlock(block2);
        Block uncle32 = blockGenerator.createChildBlock(block2);

        store.saveBlock(genesis, BigInteger.ONE, true);
        store.saveBlock(block1, BigInteger.ONE, true);
        store.saveBlock(uncle11, BigInteger.ONE, false);
        store.saveBlock(uncle12, BigInteger.ONE, false);
        store.saveBlock(block2, BigInteger.ONE, true);
        store.saveBlock(uncle21, BigInteger.ONE, false);
        store.saveBlock(uncle22, BigInteger.ONE, false);
        store.saveBlock(block3, BigInteger.ONE, true);
        store.saveBlock(uncle31, BigInteger.ONE, false);
        store.saveBlock(uncle32, BigInteger.ONE, false);

        Set<ByteArrayWrapper> family = FamilyUtils.getFamily(store, block3, 2);

        Assert.assertNotNull(family);
        Assert.assertFalse(family.isEmpty());
        Assert.assertEquals(4, family.size());

        Assert.assertFalse(family.contains(genesis.getWrappedHash()));
        Assert.assertTrue(family.contains(block1.getWrappedHash()));
        Assert.assertFalse(family.contains(uncle11.getWrappedHash()));
        Assert.assertFalse(family.contains(uncle12.getWrappedHash()));
        Assert.assertTrue(family.contains(block2.getWrappedHash()));
        Assert.assertTrue(family.contains(uncle21.getWrappedHash()));
        Assert.assertTrue(family.contains(uncle22.getWrappedHash()));
        Assert.assertFalse(family.contains(block3.getWrappedHash()));
        Assert.assertFalse(family.contains(uncle31.getWrappedHash()));
        Assert.assertFalse(family.contains(uncle32.getWrappedHash()));

        family = FamilyUtils.getFamily(store, block3, 3);

        Assert.assertNotNull(family);
        Assert.assertFalse(family.isEmpty());
        Assert.assertEquals(7, family.size());

        Assert.assertTrue(family.contains(genesis.getWrappedHash()));
        Assert.assertTrue(family.contains(block1.getWrappedHash()));
        Assert.assertTrue(family.contains(uncle11.getWrappedHash()));
        Assert.assertTrue(family.contains(uncle12.getWrappedHash()));
        Assert.assertTrue(family.contains(block2.getWrappedHash()));
        Assert.assertTrue(family.contains(uncle21.getWrappedHash()));
        Assert.assertTrue(family.contains(uncle22.getWrappedHash()));
        Assert.assertFalse(family.contains(block3.getWrappedHash()));
        Assert.assertFalse(family.contains(uncle31.getWrappedHash()));
        Assert.assertFalse(family.contains(uncle32.getWrappedHash()));
    }

    @Test
    public void getUnclesHeaders() {
        BlockStore store = createBlockStore();

        BlockGenerator blockGenerator = new BlockGenerator();
        Block genesis = blockGenerator.getGenesisBlock();
        Block block1 = blockGenerator.createChildBlock(genesis);
        Block uncle11 = blockGenerator.createChildBlock(genesis);
        Block uncle111 = blockGenerator.createChildBlock(uncle11);
        Block uncle12 = blockGenerator.createChildBlock(genesis);
        Block uncle121 = blockGenerator.createChildBlock(uncle12);
        Block block2 = blockGenerator.createChildBlock(block1);
        Block uncle21 = blockGenerator.createChildBlock(block1);
        Block uncle22 = blockGenerator.createChildBlock(block1);
        Block block3 = blockGenerator.createChildBlock(block2);
        Block uncle31 = blockGenerator.createChildBlock(block2);
        Block uncle32 = blockGenerator.createChildBlock(block2);

        store.saveBlock(genesis, BigInteger.ONE, true);
        store.saveBlock(block1, BigInteger.ONE, true);
        store.saveBlock(uncle11, BigInteger.ONE, false);
        store.saveBlock(uncle12, BigInteger.ONE, false);
        store.saveBlock(uncle111, BigInteger.ONE, false);
        store.saveBlock(uncle121, BigInteger.ONE, false);
        store.saveBlock(block2, BigInteger.ONE, true);
        store.saveBlock(uncle21, BigInteger.ONE, false);
        store.saveBlock(uncle22, BigInteger.ONE, false);
        store.saveBlock(block3, BigInteger.ONE, true);
        store.saveBlock(uncle31, BigInteger.ONE, false);
        store.saveBlock(uncle32, BigInteger.ONE, false);

        List<BlockHeader> list = FamilyUtils.getUnclesHeaders(store, block3, 3);

        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertEquals(4, list.size());

        Assert.assertTrue(containsHash(uncle11.getHash(), list));
        Assert.assertTrue(containsHash(uncle12.getHash(), list));
        Assert.assertTrue(containsHash(uncle21.getHash(), list));
        Assert.assertTrue(containsHash(uncle22.getHash(), list));
    }

    @Test
    public void getUncles() {
        BlockStore store = createBlockStore();

        BlockGenerator blockGenerator = new BlockGenerator();
        Block genesis = blockGenerator.getGenesisBlock();
        Block block1 = blockGenerator.createChildBlock(genesis);
        Block uncle11 = blockGenerator.createChildBlock(genesis);
        Block uncle12 = blockGenerator.createChildBlock(genesis);
        Block block2 = blockGenerator.createChildBlock(block1);
        Block uncle21 = blockGenerator.createChildBlock(block1);
        Block uncle22 = blockGenerator.createChildBlock(block1);
        Block block3 = blockGenerator.createChildBlock(block2);
        Block uncle31 = blockGenerator.createChildBlock(block2);
        Block uncle32 = blockGenerator.createChildBlock(block2);

        store.saveBlock(genesis, BigInteger.ONE, true);
        store.saveBlock(block1, BigInteger.ONE, true);
        store.saveBlock(uncle11, BigInteger.ONE, false);
        store.saveBlock(uncle12, BigInteger.ONE, false);
        store.saveBlock(block2, BigInteger.ONE, true);
        store.saveBlock(uncle21, BigInteger.ONE, false);
        store.saveBlock(uncle22, BigInteger.ONE, false);
        store.saveBlock(block3, BigInteger.ONE, true);
        store.saveBlock(uncle31, BigInteger.ONE, false);
        store.saveBlock(uncle32, BigInteger.ONE, false);

        Set<ByteArrayWrapper> family = FamilyUtils.getUncles(store, block3, 3);

        Assert.assertNotNull(family);
        Assert.assertFalse(family.isEmpty());
        Assert.assertEquals(4, family.size());

        Assert.assertFalse(family.contains(genesis.getWrappedHash()));
        Assert.assertFalse(family.contains(block1.getWrappedHash()));
        Assert.assertTrue(family.contains(uncle11.getWrappedHash()));
        Assert.assertTrue(family.contains(uncle12.getWrappedHash()));
        Assert.assertFalse(family.contains(block2.getWrappedHash()));
        Assert.assertTrue(family.contains(uncle21.getWrappedHash()));
        Assert.assertTrue(family.contains(uncle22.getWrappedHash()));
        Assert.assertFalse(family.contains(block3.getWrappedHash()));
        Assert.assertFalse(family.contains(uncle31.getWrappedHash()));
        Assert.assertFalse(family.contains(uncle32.getWrappedHash()));
    }

    private static BlockStore createBlockStore() {
        return new IndexedBlockStore(new HashMap<>(), new HashMapDB(), null);
    }

    private static boolean containsHash(byte[] hash, List<BlockHeader> headers) {
        for (BlockHeader header : headers)
            if (Arrays.equals(hash, header.getHash()))
                return true;

        return false;
    }
}
