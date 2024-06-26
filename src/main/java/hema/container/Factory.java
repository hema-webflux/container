package hema.container;

import java.lang.reflect.Parameter;

interface Factory {

    /**
     * Create a new resolver instance.
     *
     * @param parameter Reflector parameter object.
     *
     * @return Resolver instance.
     */
    Resolver make(Parameter parameter);

    void setContainer(Container container);
}
