package com.dts.classes;

public class InventarioHelper {

    public static boolean tagExists(String tag, int tipoInv, clsInventario_detalleObj invDet, clsInventario_ciegoObj invCiego) {
        if (tag == null || tag.isEmpty()) return false;

        if (tipoInv == 2) {
            return invDet.existsByCodigoBarra(tag);
        } else if (tipoInv == 1) {
            return invCiego.existsByCodigoBarra(tag);
        }

        return false;
    }
}
