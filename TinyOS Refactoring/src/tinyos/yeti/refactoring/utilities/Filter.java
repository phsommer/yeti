package tinyos.yeti.refactoring.utilities;

/**
 * Interface to Filter Objects.
 * @param <T>
 */
public interface Filter<T> {
	boolean test(T toTest);
}
