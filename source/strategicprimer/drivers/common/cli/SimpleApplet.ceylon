shared class SimpleApplet<Arguments=[]>(shared actual Anything(*Arguments) invoke,
    shared actual String description,
    shared actual String+ commands) satisfies Applet<Arguments>
        given Arguments satisfies Anything[] {}
