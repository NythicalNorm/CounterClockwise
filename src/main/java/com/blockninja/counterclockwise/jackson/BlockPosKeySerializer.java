package com.blockninja.counterclockwise.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.minecraft.core.BlockPos;

import java.io.IOException;

public class BlockPosKeySerializer extends JsonSerializer<BlockPos> {
    @Override
    public void serialize(BlockPos pos, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeFieldName(Long.toString(pos.asLong()));
    }
}
