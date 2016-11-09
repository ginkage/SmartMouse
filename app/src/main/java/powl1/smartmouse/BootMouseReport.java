package powl1.smartmouse;

class BootMouseReport {
    private boolean button1;
    private boolean button2;
    private boolean button3;
    private int xDisplacement;
    private int yDisplacement;

    byte[] getRawValue() {
        byte[] value = new byte[4];
        int button = 0;
        if (this.button1) {
            button |= 1;
        }
        if (this.button2) {
            button |= 2;
        }
        if (this.button3) {
            button |= 4;
        }
        value[0] = (byte) button;
        value[1] = (byte) this.xDisplacement;
        value[2] = (byte) this.yDisplacement;
        return value;
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
