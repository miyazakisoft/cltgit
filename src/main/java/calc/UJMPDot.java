package calc;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;

public class UJMPDot extends Dot{
  Matrix x,y;

  UJMPDot(){
    x = DenseMatrix.Factory.linkToArray(getInitArray());
    y = DenseMatrix.Factory.linkToArray(getInitArray());
  }

  public void perform(){
    Matrix result = x.mtimes(y);
  }
}
