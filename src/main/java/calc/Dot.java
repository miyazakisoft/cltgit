package calc;
import java.util.stream.IntStream;

public abstract class Dot{
  static final Integer N = 3000;

  public static double[][] getInitArray(){
    double array[][] = new double[N][N];
    IntStream.range(0, N).forEach(i -> {
               IntStream.range(0, N).forEach(j -> {
                    array[i][j] = (double)i;
               });
    });
    return array;
  }

  public abstract void perform();
}
