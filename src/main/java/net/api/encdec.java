package net.api;

import java.util.Arrays;

public class encdec implements MessageEncoderDecoder<String> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;

    @Override
    public String decodeNextByte(byte nextByte) {
        if (nextByte == ';') {
            return popString();
        }

        pushByte(nextByte);
        return null; //not a line yet
    }

    @Override
    public byte[] encode(String message) {
        return new byte[0];
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }
    private String popString() {
        //get op and work accordingly
        short op = bytesToShort(Arrays.copyOfRange(bytes,0,2));
        String result ="";
        switch (op) {
            case 1: //Register
            case 5: //Post message
            case 6: //PM message
            case 8: //STAT message
            case 12: //Block Message
                result = String.valueOf(op) + " " + splitByZero(Arrays.copyOfRange(bytes, 2, bytes.length));
                break;
            case 2:
                result = String.valueOf(op) + " " + splitByZero(Arrays.copyOfRange(bytes, 2, bytes.length - 1)) + " " + bytes[bytes.length - 1]; //Login
            break;
            case 3: // Logout
            case 7: // Logstat
                result = String.valueOf(op);
                break;
            case 4: //Follow/Unfollow
                result = String.valueOf(op) + " " + bytes[2] + " " + new String(bytes, 3, bytes.length);
            break;
        }
        len = 0;
        return result;
    }
    public short bytesToShort(byte[] byteArr) {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
    private String splitByZero(byte[] byteArr){
        String str = "";
        //str += bytesToShort(Arrays.copyOfRange(byteArr, 0, 2));
        int index = 0;
        for (int i = 0; i < byteArr.length; i++) {
            if(byteArr[i]==0){
                str+= " " + new String(byteArr,index, i - index);
                index= i+1;
            }
        }
        return str;
    }
}
