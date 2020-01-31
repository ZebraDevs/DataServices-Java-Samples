package com.zebra.barcodeintelligencetools;

public class BarcodeHelpers {
    /**
     * Converts an EAN8 barcode to a UPC-A.
     *
     * @param ean8 The EAN8 barcode to convert.
     * @return The derived UPC-A barcode.
     * @throws Exception Thrown when the provided input is not an EAN8.
     */
    public static String ean8ToUPCA(String ean8) throws Exception {
        if (ean8.length() < 8) {
            ean8 = eanChecksum(ean8);
        }
        if ("012".contains(Character.toString(ean8.charAt(6)))) {
            return ean8.substring(0, 3) + ean8.charAt(6) + "0000" + ean8.substring(3, 6) + ean8.charAt(7);
        }
        if (ean8.charAt(6) == '3') {
            return ean8.substring(0, 4) + "00000" + ean8.substring(4, 6) + ean8.charAt(7);
        }
        if (ean8.charAt(6) == '4') {
            return ean8.substring(0, 5) + "00000" + ean8.charAt(5) + ean8.charAt(7);
        }
        if ("56789".contains(Character.toString(ean8.charAt(6)))) {
            return ean8.substring(0, 6) + "0000" + ean8.substring(6);
        }
        throw new Exception("Invalid EAN8 barcode.");
    }

    /**
     * Calculates the checksum digit for an EAN8 barcode.
     *
     * @param code A 6- or 7-digit fragment of the EAN8 barcode without the checksum, or an 8-digit EAN to verify its checksum.
     * @return The full EAN8 barcode.
     * @throws Exception Thrown if the EAN8 is invalid.
     */
    public static String eanChecksum(String code) throws Exception {
        if (code.length() == 6) {
            code = "0" + code;
        }
        int[] barcode = new int[code.length()];
        for (int i = 0; i < barcode.length; i++)
            barcode[i] = Integer.parseInt(Character.toString(code.charAt(i)));
        int sum1 = barcode[1] + barcode[3] + barcode[5];
        int sum2 = 3 * (barcode[0] + barcode[2] + barcode[4] + barcode[6]);
        int checksum_value = sum1 + sum2;

        int checksum_digit = 10 - (checksum_value % 10);
        if (checksum_digit == 10) {
            checksum_digit = 0;
        }
        if (barcode.length == 8 && barcode[7] != checksum_digit) {
            throw new Exception("Provided checksum digit " + barcode[7] + " does not match expected checksum of " + checksum_digit + ".");
        }
        return code + checksum_digit;
    }
}