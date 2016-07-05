package com.jack.nit.data.header;

import java.util.function.Predicate;

import com.pixbits.io.BinaryBuffer;

public abstract class Test
{
  abstract Predicate<BinaryBuffer> build();
}
