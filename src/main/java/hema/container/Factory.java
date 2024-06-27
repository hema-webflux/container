package hema.container;

public interface Factory<R, P> {

    R make(P parameter);
}