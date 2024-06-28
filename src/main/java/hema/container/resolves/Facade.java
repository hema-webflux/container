package hema.container.resolves;

import hema.container.Resolver;

interface Facade extends Resolver {

    /**
     * Get the registered name of the component.
     *
     * @return string
     */
    String getFacadeAccessor();

}
