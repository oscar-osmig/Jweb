package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * JavaScript Proxy and Reflect APIs for meta-programming.
 *
 * <p>The Proxy object enables creation of proxies for other objects, allowing
 * interception and customization of fundamental operations (property lookup,
 * assignment, enumeration, function invocation, etc.).</p>
 *
 * <p>The Reflect API provides methods for interceptable JavaScript operations,
 * providing a clean and consistent way to interact with objects.</p>
 *
 * <p>Usage Examples:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSProxy.*;
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * // Simple validation proxy
 * proxy(variable("target"))
 *     .onSet(callback("obj", "prop", "value")
 *         .if_(variable("prop").eq("age").and(variable("value").lt(0)))
 *             .ret(false)
 *         .end()
 *         .ret(reflectSet(variable("obj"), variable("prop"), variable("value")))
 *     )
 *     .build("p")
 *
 * // Logging proxy
 * proxy(variable("obj"))
 *     .onGet(callback("target", "prop")
 *         .log(str("Getting: "), variable("prop"))
 *         .ret(reflectGet(variable("target"), variable("prop")))
 *     )
 *     .build("logged")
 *
 * // Revocable proxy
 * proxyRevocable(variable("target"), variable("handler")).build("revocable")
 * // Later: revoke(variable("revocable"))
 * </pre>
 */
public final class JSProxy {
    private JSProxy() {}

    // ==================== Proxy Creation ====================

    /**
     * Creates a new Proxy with handler.
     * @param target the target object to proxy
     * @return a ProxyBuilder to configure handlers
     */
    public static ProxyBuilder proxy(Val target) {
        return new ProxyBuilder(target);
    }

    /**
     * Creates a revocable Proxy.
     * Returns {proxy, revoke} where proxy is the proxy object and revoke is a function
     * that can be called to revoke the proxy.
     *
     * @param target the target object
     * @param handler the handler object
     * @return a Val representing Proxy.revocable(target, handler)
     */
    public static Val proxyRevocable(Val target, Val handler) {
        return new Val("Proxy.revocable(" + target.js() + "," + handler.js() + ")");
    }

    /**
     * Revokes a revocable proxy.
     * @param revocableProxy the revocable proxy object (has .revoke() method)
     * @return a Val representing revocableProxy.revoke()
     */
    public static Val revoke(Val revocableProxy) {
        return new Val(revocableProxy.js() + ".revoke()");
    }

    // ==================== Reflect API - Property Operations ====================

    /**
     * Gets a property value: Reflect.get(target, property)
     * @param target the target object
     * @param property the property name
     * @return a Val representing the property value
     */
    public static Val reflectGet(Val target, Val property) {
        return new Val("Reflect.get(" + target.js() + "," + property.js() + ")");
    }

    /**
     * Gets a property value with static property name: Reflect.get(target, 'property')
     */
    public static Val reflectGet(Val target, String property) {
        return new Val("Reflect.get(" + target.js() + ",'" + JS.esc(property) + "')");
    }

    /**
     * Gets a property value with receiver: Reflect.get(target, property, receiver)
     * @param target the target object
     * @param property the property name
     * @param receiver the value of 'this' provided for the call
     * @return a Val representing the property value
     */
    public static Val reflectGet(Val target, Val property, Val receiver) {
        return new Val("Reflect.get(" + target.js() + "," + property.js() + "," + receiver.js() + ")");
    }

    /**
     * Sets a property value: Reflect.set(target, property, value)
     * @param target the target object
     * @param property the property name
     * @param value the value to set
     * @return a Val representing a boolean (true if successful)
     */
    public static Val reflectSet(Val target, Val property, Val value) {
        return new Val("Reflect.set(" + target.js() + "," + property.js() + "," + value.js() + ")");
    }

    /**
     * Sets a property value with static property name: Reflect.set(target, 'property', value)
     */
    public static Val reflectSet(Val target, String property, Val value) {
        return new Val("Reflect.set(" + target.js() + ",'" + JS.esc(property) + "'," + value.js() + ")");
    }

    /**
     * Sets a property value with receiver: Reflect.set(target, property, value, receiver)
     */
    public static Val reflectSet(Val target, Val property, Val value, Val receiver) {
        return new Val("Reflect.set(" + target.js() + "," + property.js() + "," + value.js() + "," + receiver.js() + ")");
    }

    /**
     * Checks if object has a property: Reflect.has(target, property)
     * @return a Val representing a boolean
     */
    public static Val reflectHas(Val target, Val property) {
        return new Val("Reflect.has(" + target.js() + "," + property.js() + ")");
    }

    /**
     * Checks if object has a property with static name: Reflect.has(target, 'property')
     */
    public static Val reflectHas(Val target, String property) {
        return new Val("Reflect.has(" + target.js() + ",'" + JS.esc(property) + "')");
    }

    /**
     * Deletes a property: Reflect.deleteProperty(target, property)
     * @return a Val representing a boolean (true if successful)
     */
    public static Val reflectDeleteProperty(Val target, Val property) {
        return new Val("Reflect.deleteProperty(" + target.js() + "," + property.js() + ")");
    }

    /**
     * Deletes a property with static name: Reflect.deleteProperty(target, 'property')
     */
    public static Val reflectDeleteProperty(Val target, String property) {
        return new Val("Reflect.deleteProperty(" + target.js() + ",'" + JS.esc(property) + "')");
    }

    // ==================== Reflect API - Function Operations ====================

    /**
     * Calls a function: Reflect.apply(fn, thisArg, args)
     * @param fn the function to call
     * @param thisArg the value of 'this' for the call
     * @param args the arguments array
     * @return a Val representing the function result
     */
    public static Val reflectApply(Val fn, Val thisArg, Val args) {
        return new Val("Reflect.apply(" + fn.js() + "," + thisArg.js() + "," + args.js() + ")");
    }

    /**
     * Calls a constructor: Reflect.construct(target, args)
     * @param target the constructor to call
     * @param args the arguments array
     * @return a Val representing the new instance
     */
    public static Val reflectConstruct(Val target, Val args) {
        return new Val("Reflect.construct(" + target.js() + "," + args.js() + ")");
    }

    /**
     * Calls a constructor with newTarget: Reflect.construct(target, args, newTarget)
     * @param target the constructor to call
     * @param args the arguments array
     * @param newTarget the constructor whose prototype should be used
     * @return a Val representing the new instance
     */
    public static Val reflectConstruct(Val target, Val args, Val newTarget) {
        return new Val("Reflect.construct(" + target.js() + "," + args.js() + "," + newTarget.js() + ")");
    }

    // ==================== Reflect API - Prototype Operations ====================

    /**
     * Gets the prototype: Reflect.getPrototypeOf(target)
     * @return a Val representing the prototype object or null
     */
    public static Val reflectGetPrototypeOf(Val target) {
        return new Val("Reflect.getPrototypeOf(" + target.js() + ")");
    }

    /**
     * Sets the prototype: Reflect.setPrototypeOf(target, proto)
     * @param target the target object
     * @param proto the new prototype (or null)
     * @return a Val representing a boolean (true if successful)
     */
    public static Val reflectSetPrototypeOf(Val target, Val proto) {
        return new Val("Reflect.setPrototypeOf(" + target.js() + "," + proto.js() + ")");
    }

    // ==================== Reflect API - Extensibility ====================

    /**
     * Checks if object is extensible: Reflect.isExtensible(target)
     * @return a Val representing a boolean
     */
    public static Val reflectIsExtensible(Val target) {
        return new Val("Reflect.isExtensible(" + target.js() + ")");
    }

    /**
     * Prevents extensions: Reflect.preventExtensions(target)
     * @return a Val representing a boolean (true if successful)
     */
    public static Val reflectPreventExtensions(Val target) {
        return new Val("Reflect.preventExtensions(" + target.js() + ")");
    }

    // ==================== Reflect API - Property Descriptors ====================

    /**
     * Gets property descriptor: Reflect.getOwnPropertyDescriptor(target, property)
     * @return a Val representing the descriptor object or undefined
     */
    public static Val reflectGetOwnPropertyDescriptor(Val target, Val property) {
        return new Val("Reflect.getOwnPropertyDescriptor(" + target.js() + "," + property.js() + ")");
    }

    /**
     * Gets property descriptor with static name: Reflect.getOwnPropertyDescriptor(target, 'property')
     */
    public static Val reflectGetOwnPropertyDescriptor(Val target, String property) {
        return new Val("Reflect.getOwnPropertyDescriptor(" + target.js() + ",'" + JS.esc(property) + "')");
    }

    /**
     * Defines a property: Reflect.defineProperty(target, property, descriptor)
     * @param target the target object
     * @param property the property name
     * @param descriptor the property descriptor object
     * @return a Val representing a boolean (true if successful)
     */
    public static Val reflectDefineProperty(Val target, Val property, Val descriptor) {
        return new Val("Reflect.defineProperty(" + target.js() + "," + property.js() + "," + descriptor.js() + ")");
    }

    /**
     * Defines a property with static name: Reflect.defineProperty(target, 'property', descriptor)
     */
    public static Val reflectDefineProperty(Val target, String property, Val descriptor) {
        return new Val("Reflect.defineProperty(" + target.js() + ",'" + JS.esc(property) + "'," + descriptor.js() + ")");
    }

    /**
     * Gets all own property keys: Reflect.ownKeys(target)
     * Returns an array of the target's own (not inherited) property keys.
     * @return a Val representing the keys array
     */
    public static Val reflectOwnKeys(Val target) {
        return new Val("Reflect.ownKeys(" + target.js() + ")");
    }

    // ==================== Property Descriptor Builders ====================

    /**
     * Creates a data property descriptor.
     * @return a new PropertyDescriptorBuilder
     */
    public static PropertyDescriptorBuilder dataDescriptor() {
        return new PropertyDescriptorBuilder(false);
    }

    /**
     * Creates an accessor property descriptor.
     * @return a new PropertyDescriptorBuilder
     */
    public static PropertyDescriptorBuilder accessorDescriptor() {
        return new PropertyDescriptorBuilder(true);
    }

    /**
     * Builder for property descriptors.
     */
    public static class PropertyDescriptorBuilder {
        private final boolean isAccessor;
        private Val value;
        private Val getter;
        private Val setter;
        private Boolean writable;
        private Boolean enumerable;
        private Boolean configurable;

        PropertyDescriptorBuilder(boolean isAccessor) {
            this.isAccessor = isAccessor;
        }

        /** Sets the value (data descriptor only) */
        public PropertyDescriptorBuilder value(Val value) {
            if (isAccessor) throw new IllegalStateException("Cannot set value on accessor descriptor");
            this.value = value;
            return this;
        }

        /** Sets the value with static data (data descriptor only) */
        public PropertyDescriptorBuilder value(Object value) {
            if (isAccessor) throw new IllegalStateException("Cannot set value on accessor descriptor");
            this.value = new Val(JS.toJs(value));
            return this;
        }

        /** Sets the getter function (accessor descriptor only) */
        public PropertyDescriptorBuilder get(Func getter) {
            if (!isAccessor) throw new IllegalStateException("Cannot set getter on data descriptor");
            this.getter = new Val(getter.toExpr());
            return this;
        }

        /** Sets the setter function (accessor descriptor only) */
        public PropertyDescriptorBuilder set(Func setter) {
            if (!isAccessor) throw new IllegalStateException("Cannot set setter on data descriptor");
            this.setter = new Val(setter.toExpr());
            return this;
        }

        /** Sets writable flag (data descriptor only) */
        public PropertyDescriptorBuilder writable(boolean writable) {
            if (isAccessor) throw new IllegalStateException("Cannot set writable on accessor descriptor");
            this.writable = writable;
            return this;
        }

        /** Sets enumerable flag */
        public PropertyDescriptorBuilder enumerable(boolean enumerable) {
            this.enumerable = enumerable;
            return this;
        }

        /** Sets configurable flag */
        public PropertyDescriptorBuilder configurable(boolean configurable) {
            this.configurable = configurable;
            return this;
        }

        /** Builds the descriptor object */
        public Val build() {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;

            if (isAccessor) {
                if (getter != null) {
                    sb.append("get:").append(getter.js());
                    first = false;
                }
                if (setter != null) {
                    if (!first) sb.append(",");
                    sb.append("set:").append(setter.js());
                    first = false;
                }
            } else {
                if (value != null) {
                    sb.append("value:").append(value.js());
                    first = false;
                }
                if (writable != null) {
                    if (!first) sb.append(",");
                    sb.append("writable:").append(writable);
                    first = false;
                }
            }

            if (enumerable != null) {
                if (!first) sb.append(",");
                sb.append("enumerable:").append(enumerable);
                first = false;
            }
            if (configurable != null) {
                if (!first) sb.append(",");
                sb.append("configurable:").append(configurable);
            }

            return new Val(sb.append("}").toString());
        }
    }

    // ==================== Proxy Handler Builder ====================

    /**
     * Builder for creating Proxy objects with handler traps.
     */
    public static class ProxyBuilder {
        private final Val target;
        private Func getTrap;
        private Func setTrap;
        private Func hasTrap;
        private Func deletePropertyTrap;
        private Func applyTrap;
        private Func constructTrap;
        private Func getPrototypeOfTrap;
        private Func setPrototypeOfTrap;
        private Func isExtensibleTrap;
        private Func preventExtensionsTrap;
        private Func getOwnPropertyDescriptorTrap;
        private Func definePropertyTrap;
        private Func ownKeysTrap;

        ProxyBuilder(Val target) {
            this.target = target;
        }

        /**
         * Sets the get trap: get(target, property, receiver)
         * Intercepts property access.
         */
        public ProxyBuilder onGet(Func handler) {
            this.getTrap = handler;
            return this;
        }

        /**
         * Sets the set trap: set(target, property, value, receiver)
         * Intercepts property assignment. Should return true on success, false on failure.
         */
        public ProxyBuilder onSet(Func handler) {
            this.setTrap = handler;
            return this;
        }

        /**
         * Sets the has trap: has(target, property)
         * Intercepts the 'in' operator. Should return a boolean.
         */
        public ProxyBuilder onHas(Func handler) {
            this.hasTrap = handler;
            return this;
        }

        /**
         * Sets the deleteProperty trap: deleteProperty(target, property)
         * Intercepts the 'delete' operator. Should return a boolean.
         */
        public ProxyBuilder onDeleteProperty(Func handler) {
            this.deletePropertyTrap = handler;
            return this;
        }

        /**
         * Sets the apply trap: apply(target, thisArg, argumentsList)
         * Intercepts function calls.
         */
        public ProxyBuilder onApply(Func handler) {
            this.applyTrap = handler;
            return this;
        }

        /**
         * Sets the construct trap: construct(target, argumentsList, newTarget)
         * Intercepts the 'new' operator. Should return an object.
         */
        public ProxyBuilder onConstruct(Func handler) {
            this.constructTrap = handler;
            return this;
        }

        /**
         * Sets the getPrototypeOf trap: getPrototypeOf(target)
         * Intercepts Object.getPrototypeOf(). Should return an object or null.
         */
        public ProxyBuilder onGetPrototypeOf(Func handler) {
            this.getPrototypeOfTrap = handler;
            return this;
        }

        /**
         * Sets the setPrototypeOf trap: setPrototypeOf(target, proto)
         * Intercepts Object.setPrototypeOf(). Should return a boolean.
         */
        public ProxyBuilder onSetPrototypeOf(Func handler) {
            this.setPrototypeOfTrap = handler;
            return this;
        }

        /**
         * Sets the isExtensible trap: isExtensible(target)
         * Intercepts Object.isExtensible(). Should return a boolean.
         */
        public ProxyBuilder onIsExtensible(Func handler) {
            this.isExtensibleTrap = handler;
            return this;
        }

        /**
         * Sets the preventExtensions trap: preventExtensions(target)
         * Intercepts Object.preventExtensions(). Should return a boolean.
         */
        public ProxyBuilder onPreventExtensions(Func handler) {
            this.preventExtensionsTrap = handler;
            return this;
        }

        /**
         * Sets the getOwnPropertyDescriptor trap: getOwnPropertyDescriptor(target, property)
         * Intercepts Object.getOwnPropertyDescriptor(). Should return a descriptor or undefined.
         */
        public ProxyBuilder onGetOwnPropertyDescriptor(Func handler) {
            this.getOwnPropertyDescriptorTrap = handler;
            return this;
        }

        /**
         * Sets the defineProperty trap: defineProperty(target, property, descriptor)
         * Intercepts Object.defineProperty(). Should return a boolean.
         */
        public ProxyBuilder onDefineProperty(Func handler) {
            this.definePropertyTrap = handler;
            return this;
        }

        /**
         * Sets the ownKeys trap: ownKeys(target)
         * Intercepts Object.getOwnPropertyNames() and Object.getOwnPropertySymbols().
         * Should return an array.
         */
        public ProxyBuilder onOwnKeys(Func handler) {
            this.ownKeysTrap = handler;
            return this;
        }

        /**
         * Builds the handler object.
         */
        private String buildHandler() {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;

            if (getTrap != null) {
                sb.append("get:").append(getTrap.toExpr());
                first = false;
            }
            if (setTrap != null) {
                if (!first) sb.append(",");
                sb.append("set:").append(setTrap.toExpr());
                first = false;
            }
            if (hasTrap != null) {
                if (!first) sb.append(",");
                sb.append("has:").append(hasTrap.toExpr());
                first = false;
            }
            if (deletePropertyTrap != null) {
                if (!first) sb.append(",");
                sb.append("deleteProperty:").append(deletePropertyTrap.toExpr());
                first = false;
            }
            if (applyTrap != null) {
                if (!first) sb.append(",");
                sb.append("apply:").append(applyTrap.toExpr());
                first = false;
            }
            if (constructTrap != null) {
                if (!first) sb.append(",");
                sb.append("construct:").append(constructTrap.toExpr());
                first = false;
            }
            if (getPrototypeOfTrap != null) {
                if (!first) sb.append(",");
                sb.append("getPrototypeOf:").append(getPrototypeOfTrap.toExpr());
                first = false;
            }
            if (setPrototypeOfTrap != null) {
                if (!first) sb.append(",");
                sb.append("setPrototypeOf:").append(setPrototypeOfTrap.toExpr());
                first = false;
            }
            if (isExtensibleTrap != null) {
                if (!first) sb.append(",");
                sb.append("isExtensible:").append(isExtensibleTrap.toExpr());
                first = false;
            }
            if (preventExtensionsTrap != null) {
                if (!first) sb.append(",");
                sb.append("preventExtensions:").append(preventExtensionsTrap.toExpr());
                first = false;
            }
            if (getOwnPropertyDescriptorTrap != null) {
                if (!first) sb.append(",");
                sb.append("getOwnPropertyDescriptor:").append(getOwnPropertyDescriptorTrap.toExpr());
                first = false;
            }
            if (definePropertyTrap != null) {
                if (!first) sb.append(",");
                sb.append("defineProperty:").append(definePropertyTrap.toExpr());
                first = false;
            }
            if (ownKeysTrap != null) {
                if (!first) sb.append(",");
                sb.append("ownKeys:").append(ownKeysTrap.toExpr());
            }

            return sb.append("}").toString();
        }

        /**
         * Builds the proxy and assigns it to a variable.
         * @param varName the variable name to assign the proxy to
         * @return a Val representing the complete proxy assignment
         */
        public Val build(String varName) {
            return new Val("var " + varName + "=new Proxy(" + target.js() + "," + buildHandler() + ")");
        }

        /**
         * Builds just the Proxy constructor call without assignment.
         * @return a Val representing the Proxy constructor
         */
        public Val toVal() {
            return new Val("new Proxy(" + target.js() + "," + buildHandler() + ")");
        }
    }
}
