package org.dreamwork.db;

public interface IDirtyMarkable {
    /**
     * mark this object whether data changed or not
     * @return if any data changed, returns true. otherwise returns false
     */
    boolean isChanged ();

    /**
     * indicate whether the shadow object is enabled
     * @return returns true if the shadow object is enabled. otherwise returns false
     */
    boolean isShadowEnabled ();

    /**
     * set a flag that decides shadow is enable or not
     * @param enable enable or not
     */
    void setShadowEnabled (boolean enable);

    /**
     * commit all changed data, and set the CHANGE flag to false
     *
     * invoke this method carefully.
     *
     * @throws IllegalStateException throws if {@link #isChanged()} returns false
     */
    void commit ();

    /**
     * roll all changes back to last value.
     * <p>
     *     before this method is called, the shadow object must be active. in other word,
     *     the method {@link #setShadowEnabled(boolean)} should be called with parameter <code>true</code>
     * </p>
     *
     * <i>be careful to invoke this method carefully. it will discard all unsaved data.</i>
     *
     * @throws IllegalStateException throws if {@link #isChanged()} or ${@link #isShadowEnabled()} returns false.
     */
    void rollback ();
}