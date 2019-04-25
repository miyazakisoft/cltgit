package calc;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class ND4JDot extends Dot{
  INDArray x,y;

  ND4JDot(){
    x = Nd4j.create(getInitArray());
    y = Nd4j.create(getInitArray());
  }

  public void perform(){
    INDArray result = x.mmul(y);
  }
}
