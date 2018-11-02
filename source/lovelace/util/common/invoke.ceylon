"Given a no-argument method reference, invoke it and return whatever it does."
shared Type invoke<Type=Anything>(Type() lambda) => lambda();