import java.util.*;

public class Test {
  public void removeDuplicates(ArrayList a) {
    Object p;
    int index;
    for (int i=0; i<a.size()-1; ++i) {
      p = a.get(i);
      while ((index = a.lastIndexOf(p)) > i) {
        a.remove(index);
      }
    }
  }

  public static void main(String[] args) {
    new Test();
  }

  public Test() {
    ArrayList a = new ArrayList();
    a.add(new Integer(5));
    a.add(new Integer(6));
    a.add(new Integer(5));
    a.add(new Integer(5));
    a.add(new Integer(7));
    a.add(new Integer(6));
    a.add(new Integer(8));
    a.add(new Integer(8));
    System.out.println(a);
    removeDuplicates(a);
    System.out.println(a);
  }
}
