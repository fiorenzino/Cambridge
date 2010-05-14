package cambridge.runtime;

/**
 * User: erdinc
 * Date: Oct 31, 2009
 * Time: 5:25:32 PM
 */
public class PropertyAccessException extends Exception {
   private final Object o;
   private final String property;

   public PropertyAccessException(String message, Object o, String property) {
      super(message);

      this.o = o;
      this.property = property;
   }

   public Object getO() {
      return o;
   }

   public String getProperty() {
      return property;
   }
}
