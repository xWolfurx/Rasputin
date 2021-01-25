package net.wolfur.rasputin.database;

/**
 * Created by Wolfur on 17.10.2017.
 */
public abstract interface Callback<T> {

    public abstract void accept(T paramT);
}
