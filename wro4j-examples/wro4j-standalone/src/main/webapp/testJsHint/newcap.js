function MyConstructor() {
  console.log(this);
}

new MyConstructor(); // -> [MyConstructor]
MyConstructor();     // -> [DOMWindow]