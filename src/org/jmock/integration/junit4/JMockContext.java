package org.jmock.integration.junit4;

import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.List;

import org.jmock.auto.internal.Mockomatic;
import org.jmock.internal.AllDeclaredFields;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * A <code>JMockContext</code> is a JUnit Rule that manages JMock expectations 
 * and allowances, and asserts that expectations have been met after each test
 * has finished. To use it, add a field to the test class (note that you don't 
 * have to specify <code>@RunWith(JMock.class)</code> any more). For example,  
 * 
 * <pre>public class ATestWithSatisfiedExpectations {
 *  @Rule public final JMockContext context = new JMockContext();
 *  private final Runnable runnable = context.mock(Runnable.class);
 *     
 *  @Test
 *  public void doesSatisfyExpectations() {
 *    context.checking(new Expectations() {{
 *      oneOf (runnable).run();
 *    }});
 *          
 *    runnable.run();
 *  }
 *}</pre>
 *
 * Note that the Rule field must be declared public and as a <code>JMockContext</code>
 * (not a <code>Mockery</code>) for JUnit to recognise it, as it's checked statically.
 * 
 * @author smgf
 */

public class JMockContext extends JUnit4Mockery implements MethodRule {
    private final Mockomatic mockomatic = new Mockomatic(this);

    @Override
    public Statement apply(final Statement base, FrameworkMethod method, final Object target) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                prepare(target);
                base.evaluate();
                assertIsSatisfied();
            }

            private void prepare(final Object target) {
                final List<Field> allFields = AllDeclaredFields.in(target.getClass());
                assertOnlyOneJMockContextIn(allFields);
                fillInAutoMocks(target, allFields);
            }

            private void assertOnlyOneJMockContextIn(List<Field> allFields) {
                Field contextField = null;
                for (Field field : allFields) {
                    if (JMockContext.class.isAssignableFrom(field.getType())) {
                        if (null != contextField) {
                            fail("Test class should only have one JMockContext field, found " 
                                  + contextField.getName() + " and " + field.getName());
                        }
                        contextField = field;
                    }
                }
            }

            private void fillInAutoMocks(final Object target, List<Field> allFields) {
                mockomatic.fillIn(target, allFields);
            }
        };
    }
}
