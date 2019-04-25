package calc;
import org.la4j.Matrix;

public class La4jDot extends Dot{
  Matrix x,y;

  La4jDot(){
    x = Matrix.from2DArray(getInitArray());
    y = Matrix.from2DArray(getInitArray());
  }

  public void perform(){
    Matrix result = x.multiply(y);
  }
}
