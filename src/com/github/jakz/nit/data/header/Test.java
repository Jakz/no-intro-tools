package com.github.jakz.nit.data.header;

import java.util.function.Predicate;

import com.pixbits.lib.io.BinaryBuffer;

public abstract class Test
{
  abstract Predicate<BinaryBuffer> build();
}
