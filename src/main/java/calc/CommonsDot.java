package calc;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class CommonsDot extends Dot{
  RealMatrix x,y;

  CommonsDot(){
    x = MatrixUtils.createRealMatrix(getInitArray());
    y = MatrixUtils.createRealMatrix(getInitArray());
  }

  public void perform(){
    RealMatrix result = x.multiply(y);
  }
}
