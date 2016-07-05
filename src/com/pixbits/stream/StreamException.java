package com.pixbits.stream;

import java.util.function.*;

public final class StreamException
{

  @FunctionalInterface
  public interface Consumer_WithExceptions<T> { void accept(T t) throws Exception; }

  @FunctionalInterface
  public interface Function_WithExceptions<T, R>
  {
    R apply(T t) throws Exception;
  }

  @FunctionalInterface
  public interface Supplier_WithExceptions<T>
  {
    T get() throws Exception;
  }

  @FunctionalInterface
  public interface Runnable_WithExceptions
  {
    void accept() throws Exception;
  }
  
  @FunctionalInterface
  public interface Predicate_WithExceptions<T>
  {
    boolean test(T t) throws Exception;
  }

  public static <T> Consumer<T> rethrowConsumer(Consumer_WithExceptions<T> consumer)
  {
    return t -> {
      try
      {
        consumer.accept(t);
      } catch (Exception exception)
      {
        throwAsUnchecked(exception);
      }
    };
  }

  /**
   * .map(rethrowFunction(name -> Class.forName(name))) or
   * .map(rethrowFunction(Class::forName))
   */
  public static <T, R> Function<T, R> rethrowFunction(Function_WithExceptions<T, R> function)
  {
    return t -> {
      try
      {
        return function.apply(t);
      } catch (Exception exception)
      {
        throwAsUnchecked(exception);
        return null;
      }
    };
  }

  /**
   * rethrowSupplier(() -> new StringJoiner(new String(new byte[]{77, 97, 114,
   * 107}, "UTF-8"))),
   */
  public static <T> Supplier<T> rethrowSupplier(Supplier_WithExceptions<T> function)
  {
    return () -> {
      try
      {
        return function.get();
      } catch (Exception exception)
      {
        throwAsUnchecked(exception);
        return null;
      }
    };
  }
  
  public static <T> Predicate<T> rethrowPredicate(Predicate_WithExceptions<T> predicate)
  {
    return t -> {
      try
      {
        return predicate.test(t);
      } catch (Exception exception)
      {
        throwAsUnchecked(exception);
        return false;
      }
    };
  }

  /** uncheck(() -> Class.forName("xxx")); */
  public static void uncheck(Runnable_WithExceptions t)
  {
    try
    {
      t.accept();
    } catch (Exception exception)
    {
      throwAsUnchecked(exception);
    }
  }

  /** uncheck(() -> Class.forName("xxx")); */
  public static <R> R uncheck(Supplier_WithExceptions<R> supplier)
  {
    try
    {
      return supplier.get();
    } catch (Exception exception)
    {
      throwAsUnchecked(exception);
      return null;
    }
  }

  /** uncheck(Class::forName, "xxx"); */
  public static <T, R> R uncheck(Function_WithExceptions<T, R> function, T t)
  {
    try
    {
      return function.apply(t);
    } catch (Exception exception)
    {
      throwAsUnchecked(exception);
      return null;
    }
  }

  @SuppressWarnings ("unchecked")
  private static <E extends Throwable> void throwAsUnchecked(Exception exception) throws E { throw (E)exception; }
}
