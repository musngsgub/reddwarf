package com.sun.sgs.app;

import java.io.Serializable;

/**
 * Provides facilities for managing access to shared, persistent objects.
 * Managed objects are objects that implement the {@link ManagedObject} and
 * {@link Serializable} interfaces.  Each managed object is stored separately
 * along with all of the serializable, non-managed objects it refers to.  If a
 * managed object refers to another managed object, it must do so through an
 * instance of {@link ManagedReference}, created by the {@link #createReference
 * createReference} method. <p>
 *
 * Managed objects that are bound to names, and any managed objects they refer
 * to directly or indirectly, are stored by the <code>DataManager</code>.  It
 * is up to the application to determine when managed objects are no longer
 * needed and to remove them explicitly from the <code>DataManager</code> using
 * the {@link #removeObject removeObject} method.
 *
 * @see		ManagedObject
 * @see		ManagedReference
 * @see		Serializable
 */
public interface DataManager {

    /**
     * Obtains the object bound to a name.  Applications need to notify the
     * system before modifying the object or any of the non-managed objects it
     * refers to by calling {@link #markForUpdate markForUpdate} or {@link
     * ManagedReference#getForUpdate ManagedReference.getForUpdate} before
     * making the modifications.
     *
     * @param	<T> the type of the object
     * @param	name the name
     * @param	type a class representing the type of the object
     * @return	the object bound to the name
     * @throws	ClassCastException if the object bound to the name is not of
     *		the specified type
     * @throws	NameNotBoundException if no object is bound to the name
     * @throws	ObjectNotFoundException if the object bound to the name is not
     *		found
     * @throws	TransactionException if the operation failed because of a
     *		problem with the current transaction
     * @see	#markForUpdate markForUpdate
     * @see	ManagedReference#getForUpdate ManagedReference.getForUpdate
     */
    <T> T getBinding(String name, Class<T> type);

    /**
     * Binds an object to a name, replacing any previous binding.  The object,
     * as well as any objects it refers to, must implement {@link
     * Serializable}.  Note that this method will throw {@link
     * IllegalArgumentException} if <code>object</code> does not implement
     * <code>Serializable</code>, but is not guaranteed to check that all
     * referred to objects implement <code>Serializable</code>.  Any instances
     * of {@link ManagedObject} that <code>object</code> refers to directly, or
     * indirectly through non-managed objects, need to be referred to through
     * instances of {@link ManagedReference}.
     *
     * @param	name the name
     * @param	object the object
     * @throws	IllegalArgumentException if <code>object</code> does not
     *		implement {@link Serializable}
     * @throws	ObjectNotFoundException if the object has been removed
     * @throws	TransactionException if the operation failed because of a
     *		problem with the current transaction
     */
    void setBinding(String name, ManagedObject object);

    /**
     * Removes the binding for a name.  Note that the object previously bound
     * to the name, if any, is not removed; only the binding between the name
     * and the object is removed.  To remove the object, use the {@link
     * #removeObject removeObject} method.
     *
     * @param	name the name
     * @throws	NameNotBoundException if the name is not bound
     * @throws	TransactionException if the operation failed because of a
     *		problem with the current transaction
     * @see	#removeObject removeObject
     */
    void removeBinding(String name);

    /**
     * Removes an object from the <code>DataManager</code>.  The system will
     * make an effort to flag subsequent references to the removed object
     * through {@link #getBinding getBinding} or {@link ManagedReference} by
     * throwing {@link ObjectNotFoundException}, although this behavior is not
     * guaranteed.
     *
     * @param	object the object
     * @throws	IllegalArgumentException if <code>object</code> does not
     *		implement {@link Serializable}
     * @throws	ObjectNotFoundException if the object has already been removed
     * @throws	TransactionException if the operation failed because of a
     *		problem with the current transaction
     */
    void removeObject(ManagedObject object);

    /**
     * Notifies the system that an object is going to be modified.
     * Applications should call this method before modifying the object or any
     * of the non-managed objects it refers to.
     *
     * @param	object the object
     * @throws	IllegalArgumentException if <code>object</code> does not
     *		implement {@link Serializable}
     * @throws	ObjectNotFoundException if the object has been removed
     * @throws	TransactionException if the operation failed because of a
     *		problem with the current transaction
     * @see	ManagedReference#getForUpdate ManagedReference.getForUpdate 
     */
    void markForUpdate(ManagedObject object);

    /**
     * Creates a managed reference to an object.  Applications should use
     * managed references when a managed object refers to another managed
     * object, either directly or indirectly through non-managed objects.
     *
     * @param	object the object
     * @return	the managed reference
     * @throws	IllegalArgumentException if <code>object</code> does not
     *		implement {@link Serializable}
     * @throws	ObjectNotFoundException if the object has been removed
     * @throws	TransactionException if the operation failed because of a
     *		problem with the current transaction
     */
    ManagedReference createReference(ManagedObject object);
}
