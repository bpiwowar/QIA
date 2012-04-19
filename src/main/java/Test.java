/**
 * @author B. Piwowarski <benjamin@bpiwowar.net>
 * @date 19/4/12
 */
import net.bpiwowar.kqp.*;

public class Test {
    public static void main(String [] args) {
        DoubleEigenMatrix m = new DoubleEigenMatrix(10, 5);

        System.out.format("Value %g%n", m.get(2,2));
    }
}
