package toylangs.sholog;

import toylangs.sholog.ast.ShologNode;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static toylangs.sholog.ast.ShologNode.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ShologEvaluatorTest {
    private static final int UNDEFINED_CODE = 127;

    private Map<String, Boolean> env;

    @Before
    public void setUp() {
        env = new HashMap<>();
        env.put("a", true);
        env.put("b", false);
        env.put("c", false);
        env.put("foo", true);
        env.put("bar", false);

        env = Collections.unmodifiableMap(env);
    }

    @Test
    public void test01_literals() {
        checkEval(lit(true), true);
        checkEval(lit(false), false);
    }

    @Test
    public void test02_variables() {
        checkEval(var("a"), true);
        checkEval(var("b"), false);
        checkEval(var("foo"), true);
    }

    @Test
    public void test03_operators_lazy() {
        // land
        checkEval(land(lit(true), lit(true)), true);
        checkEval(land(lit(true), lit(false)), false);
        checkEval(land(lit(false), lit(true)), false);
        checkEval(land(lit(false), lit(false)), false);
        // lor
        checkEval(lor(lit(true), lit(true)), true);
        checkEval(lor(lit(true), lit(false)), true);
        checkEval(lor(lit(false), lit(true)), true);
        checkEval(lor(lit(false), lit(false)), false);
    }

    @Test
    public void test04_operators_eager() {
        // eand
        checkEval(eand(lit(true), lit(true)), true);
        checkEval(eand(lit(true), lit(false)), false);
        checkEval(eand(lit(false), lit(true)), false);
        checkEval(eand(lit(false), lit(false)), false);
        // eor
        checkEval(eor(lit(true), lit(true)), true);
        checkEval(eor(lit(true), lit(false)), true);
        checkEval(eor(lit(false), lit(true)), true);
        checkEval(eor(lit(false), lit(false)), false);
        // xor
        checkEval(xor(lit(true), lit(true)), false);
        checkEval(xor(lit(true), lit(false)), true);
        checkEval(xor(lit(false), lit(true)), true);
        checkEval(xor(lit(false), lit(false)), false);
    }

    @Test
    public void test05_operators_eager_error() {
        // eand
        checkEvalError(eand(lit(true), error(1)), 1);
        checkEvalError(eand(lit(false), error(1)), 1);
        checkEvalError(eand(error(1), lit(true)), 1);
        checkEvalError(eand(error(1), lit(false)), 1);
        // eor
        checkEvalError(eor(lit(true), error(1)), 1);
        checkEvalError(eor(lit(false), error(1)), 1);
        checkEvalError(eor(error(1), lit(true)), 1);
        checkEvalError(eor(error(1), lit(false)), 1);
        // xor
        checkEvalError(xor(lit(true), error(1)), 1);
        checkEvalError(xor(lit(false), error(1)), 1);
        checkEvalError(xor(error(1), lit(true)), 1);
        checkEvalError(xor(error(1), lit(false)), 1);
    }

    @Test
    public void test06_operators_lazy_error() {
        // land
        checkEvalError(land(lit(true), error(1)), 1);
        checkEval(land(lit(false), error(1)), false);
        checkEvalError(land(error(1), lit(true)), 1);
        checkEvalError(land(error(1), lit(false)), 1);
        checkEval(land(var("b"), var("jama")), false);
        // lor
        checkEval(lor(lit(true), error(1)), true);
        checkEvalError(lor(lit(false), error(1)), 1);
        checkEvalError(lor(error(1), lit(true)), 1);
        checkEvalError(lor(error(1), lit(false)), 1);
        checkEval(lor(var("a"), var("jama")), true);
    }

    @Test
    public void test07_variables_error() {
        checkEvalError(var("jama"), UNDEFINED_CODE);
        checkEvalError(eand(var("jama"), var("b")), UNDEFINED_CODE);
        checkEvalError(eor(var("jama"), var("a")), UNDEFINED_CODE);
        checkEvalError(xor(var("jama"), var("a")), UNDEFINED_CODE);
        checkEvalError(lor(var("jama"), var("a")), UNDEFINED_CODE);
        checkEvalError(land(var("jama"), var("a")), UNDEFINED_CODE);
    }

    @Test
    public void test08_error_multiple() {
        checkEval(eand(var("a"), var("b")), false);
        checkEval(eor(var("a"), var("b")), true);
        checkEval(xor(var("a"), var("b")), true);
        checkEval(land(var("a"), var("b")), false);
        checkEval(lor(var("a"), var("b")), true);

        checkEval(xor(var("foo"), lit(true)), false);
        checkEval(xor(var("bar"), lit(true)), true);

        checkEvalError(eand(error(1), error(2)), 1);
        checkEvalError(eor(error(1), error(2)), 1);
        checkEvalError(xor(error(1), error(2)), 1);
        checkEvalError(land(error(1), error(2)), 1);
        checkEvalError(lor(error(1), error(2)), 1);

        checkEval(xor(land(var("b"), error(1)), lor(var("a"), error(2))), true);
        checkEvalError(xor(land(var("a"), error(1)), lor(var("a"), error(2))), 1);
        checkEvalError(xor(land(var("b"), error(1)), lor(var("b"), error(2))), 2);
        checkEvalError(xor(land(var("a"), error(1)), lor(var("b"), error(2))), 1);
        checkEvalError(xor(land(var("a"), error(UNDEFINED_CODE)), lor(var("b"), error(2))), UNDEFINED_CODE);
        checkEval(xor(land(var("b"), var("jama")), lor(var("a"), var("jura"))), true);

        checkEvalError(xor(eand(var("b"), error(1)), lor(var("a"), error(2))), 1);
        checkEvalError(xor(land(var("b"), error(1)), eor(var("a"), error(2))), 2);
    }


    private void checkEval(ShologNode node, boolean expected) {
        assertEquals(expected, ShologEvaluator.eval(node, env));
    }

    private void checkEvalError(ShologNode node, int expected) {
        try {
            ShologEvaluator.eval(node, env);
            fail("expected ShologException");
        }
        catch (ShologEvaluator.ShologException e) {
            assertEquals(expected, e.getCode());
        }
    }
}
