package trading.economy;

import static org.junit.Assert.*;

public class StaticTests {
	public static void testHashCode(Object... objects) {
		for(Object o : objects) {
			for(Object p : objects) {
				if(o.equals(p)) {
					assertEquals(o.hashCode(), p.hashCode());
				}
			}
		}
	}
	
	public static void testNotEquals(Object... notEqual) {
		for(Object o : notEqual) {
			for(Object p : notEqual) {
				if(o == p) {
					assertTrue(o.equals(p));
					continue;
				}
				assertFalse(o.equals(p));
				assertFalse(p.equals(o));
			}
			assertFalse(o.equals(null));
		}
	}
	
	public static void testEquals(Pair<?, ?>... equal) {
		for(Pair<?, ?> p : equal) {
			assertTrue(p.first().equals(p.second()));
			assertTrue(p.second().equals(p.first()));
		}
	}
	
	public static void testInterning(Pair<?, ?>... equal) {
		for(Pair<?, ?> p : equal) {
			assertTrue(p.first() == p.second());
		}
	}
	
	public static void testExpectedException(VoidFunction func, Class<? extends Exception> exceptionType) {
		try {
			func.execute();
			fail();
		} catch(Exception e) {
			if(!exceptionType.isAssignableFrom(exceptionType)) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static <F, S> Pair<F, S> pair(F first, S second){
		return new Pair<F, S>(first, second);
	}
	
	public static interface VoidFunction{
		void execute() throws Exception;
	}
}

