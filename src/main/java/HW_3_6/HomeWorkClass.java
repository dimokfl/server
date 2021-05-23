package HW_3_6;

public class HomeWorkClass {

    private int[] mas1;

    public int[] method1(int[] a){
        int c = 0;
        int d = 0;
        int x = 0;
        for (int i = 0; i < a.length; i++){
            c++;
            if (a[i] == 4) {
                x = a.length - c;
                break;
            }
            if (a[i] != 4){
                d++;
            }
            if (d == (a.length-1)){
                throw new RuntimeException();
            }
        }
        int[] b = new int[x];
        for (int i = c, j = 0; i < a.length; i++, j++) {
            b[j] = a[i];
        }
        return b;
    }

    public boolean method2(int[] x){
        boolean a = false;
        boolean b = false;
        boolean c = false;
        for (int j : x) {
            if (j == 1) {
                a = true;
            }
            if (j == 4) {
                b = true;
            }
            if (a && b) {
                c = true;
            }
        }
        return c;
    }
}
