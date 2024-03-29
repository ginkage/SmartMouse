package powl1.smartmouse;

class BootMouseReport {
    private boolean button1;
    private boolean button2;
    private boolean button3;
    private int xDisplacement;
    private int yDisplacement;

    byte[] getRawValue() {
        int button = 0;
        if (button1) {
            button |= 1;
        }
        if (button2) {
            button |= 2;
        }
        if (button3) {
            button |= 4;
        }
        return new byte[] { (byte) button, (byte) xDisplacement, (byte) yDisplacement, 0 };
    }

    void setButton1(boolean button1) {
        this.button1 = button1;
    }

    void setButton2(boolean button2) {
        this.button2 = button2;
    }

    void setButton3(boolean button3) {
        this.button3 = button3;
    }

    void setXDisplacement(int xDisplacement) {
        this.xDisplacement = xDisplacement;
    }

    void setYDisplacement(int yDisplacement) {
        this.yDisplacement = yDisplacement;
    }
}
