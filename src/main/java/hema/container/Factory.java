package hema.container;

import java.lang.reflect.Parameter;

interface Factory {

    Resolver make(Parameter parameter);

    void setContainer(Container container);
}
