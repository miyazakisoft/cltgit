package calc;

public class Main {
  static Dot[] dot = {new ND4JDot(),
                      new CommonsDot(),
                      new La4jDot(),
                      new UJMPDot()};

  public static void main(String[] args) {
    if(args.length<1)return;

    long start = System.currentTimeMillis();
    dot[Integer.parseInt(args[0])].perform();
    long end = System.currentTimeMillis();
    System.out.println((end - start)  + "ms");
  }
}
