package com.blockninja.counterclockwise.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.node.DoubleNode;
import net.minecraft.core.BlockPos;

import java.io.IOException;

public class BlockPosKeyDeserializer extends KeyDeserializer {
    @Override
    public BlockPos deserializeKey(String key, DeserializationContext ctxt) {
        long l = Long.parseLong(key);
        return BlockPos.of(l);
    }
}
