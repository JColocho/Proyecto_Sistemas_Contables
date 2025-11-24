package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReporteModel {
    private int idReporte;
    private int idEmpresa;
    private String tipoReporte;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private String rutaReporte;

    public int getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(int idReporte) {
        this.idReporte = idReporte;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getTipoReporte() {
        return tipoReporte;
    }

    public void setTipoReporte(String tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public String getRutaReporte() {
        return rutaReporte;
    }

    public void setRutaReporte(String rutaReporte) {
        this.rutaReporte = rutaReporte;
    }

    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // LIBRO DIARIO
    public List<Map<String, Object>> obtenerLibroDiario(LocalDate desde, LocalDate hasta, int idEmpresa) throws SQLException {
        String sql = """
            SELECT 
                p.fecha,
                p.asiento,
                p.concepto,
                c.codigo,
                c.cuenta,
                CASE 
                    WHEN dp.cargo > 0 THEN dp.cargo 
                    ELSE NULL 
                END AS cargo,
                CASE 
                    WHEN dp.abono > 0 THEN dp.abono 
                    ELSE NULL 
                END AS abono
            FROM tblpartidas p
            INNER JOIN tbldetallepartida dp ON p.idpartida = dp.idpartida
            INNER JOIN tblcatalogocuentas c ON dp.idcuenta = c.idcuenta
            WHERE p.fecha BETWEEN ? AND ? 
            AND p.idempresa = ?
            ORDER BY p.fecha, p.asiento, dp.cargo DESC, dp.abono DESC
        """;

        List<Map<String, Object>> resultado = new ArrayList<>();

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            ps.setInt(3, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("Fecha", rs.getDate("fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    fila.put("Asiento", rs.getInt("asiento"));
                    fila.put("Concepto", rs.getString("concepto"));
                    fila.put("Código", rs.getString("codigo"));
                    fila.put("Cuenta", rs.getString("cuenta"));
                    fila.put("Cargo", rs.getObject("cargo") != null ? rs.getDouble("cargo") : null);
                    fila.put("Abono", rs.getObject("abono") != null ? rs.getDouble("abono") : null);
                    resultado.add(fila);
                }
            }
        }

        return resultado;
    }

    // LIBRO MAYOR
    public List<Map<String, Object>> obtenerLibroMayor(LocalDate desde, LocalDate hasta, int idEmpresa) throws SQLException {

        String sql = """
        SELECT 
            c.idcuenta,
            c.codigo,
            c.cuenta,
            p.fecha,
            p.concepto,
            COALESCE(dp.cargo, 0) AS cargo,
            COALESCE(dp.abono, 0) AS abono
        FROM tblcatalogocuentas c
        LEFT JOIN tbldetallepartida dp ON c.idcuenta = dp.idcuenta
        LEFT JOIN tblpartidas p ON dp.idpartida = p.idpartida
            AND p.idempresa = ?
        WHERE c.idempresa = ?
        ORDER BY c.codigo, p.fecha
    """;

        List<Map<String, Object>> resultado = new ArrayList<>();

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);
            ps.setInt(2, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {

                String cuentaActual = "";
                List<Map<String, Object>> todosMovimientos = new ArrayList<>();

                while (rs.next()) {
                    String codigo = rs.getString("codigo");
                    String cuenta = rs.getString("cuenta");
                    String nombreCuenta = codigo + " - " + cuenta;

                    // Si cambia de cuenta, procesar la anterior
                    if (!nombreCuenta.equals(cuentaActual) && !cuentaActual.isEmpty()) {
                        procesarCuenta(resultado, cuentaActual, todosMovimientos, desde, hasta);
                        todosMovimientos = new ArrayList<>();
                    }

                    cuentaActual = nombreCuenta;

                    // Guardar todos los movimientos
                    LocalDate fechaMov = rs.getDate("fecha") != null
                            ? rs.getDate("fecha").toLocalDate()
                            : null;

                    if (fechaMov != null) {
                        Map<String, Object> movimiento = new LinkedHashMap<>();
                        movimiento.put("fecha", fechaMov);
                        movimiento.put("concepto", rs.getString("concepto"));
                        movimiento.put("cargo", rs.getDouble("cargo"));
                        movimiento.put("abono", rs.getDouble("abono"));
                        todosMovimientos.add(movimiento);
                    }
                }

                // Procesar última cuenta
                if (!cuentaActual.isEmpty()) {
                    procesarCuenta(resultado, cuentaActual, todosMovimientos, desde, hasta);
                }
            }
        }

        return resultado;
    }

    //Procesa una cuenta del Libro Mayor.
    private void procesarCuenta(List<Map<String, Object>> resultado,
                                String cuenta,
                                List<Map<String, Object>> todosMovimientos,
                                LocalDate desde,
                                LocalDate hasta) {

        //Calcular saldo inicial (antes del periodo)
        double saldoInicial = 0.0;

        for (Map<String, Object> mov : todosMovimientos) {
            LocalDate fecha = (LocalDate) mov.get("fecha");
            double cargo = (Double) mov.get("cargo");
            double abono = (Double) mov.get("abono");

            // Solo movimientos ANTES del periodo
            if (fecha.isBefore(desde)) {
                saldoInicial += cargo - abono;
            }
        }

        //Agregar encabezado con saldo inicial
        Map<String, Object> encabezado = new LinkedHashMap<>();
        encabezado.put("Cuenta", ">>> " + cuenta);
        encabezado.put("Fecha", "");
        encabezado.put("Concepto", "Saldo Inicial");
        encabezado.put("Cargo", null);
        encabezado.put("Abono", null);
        encabezado.put("Saldo", saldoInicial);
        resultado.add(encabezado);

        // Agregar movimientos del periodo
        double saldo = saldoInicial;

        for (Map<String, Object> mov : todosMovimientos) {
            LocalDate fecha = (LocalDate) mov.get("fecha");
            String concepto = (String) mov.get("concepto");
            double cargo = (Double) mov.get("cargo");
            double abono = (Double) mov.get("abono");

            // Solo movimientos DENTRO del periodo
            if (!fecha.isBefore(desde) && !fecha.isAfter(hasta)) {

                // Calcular nuevo saldo (UNIVERSAL)
                saldo += cargo - abono;

                // Agregar fila
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("Cuenta", "");
                fila.put("Fecha", fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                fila.put("Concepto", concepto);
                fila.put("Cargo", cargo > 0 ? cargo : null);
                fila.put("Abono", abono > 0 ? abono : null);
                fila.put("Saldo", saldo);

                resultado.add(fila);
            }
        }

        //Agregar separador
        Map<String, Object> separador = new LinkedHashMap<>();
        separador.put("Cuenta", "");
        separador.put("Fecha", "");
        separador.put("Concepto", "");
        separador.put("Cargo", null);
        separador.put("Abono", null);
        separador.put("Saldo", null);
        resultado.add(separador);
    }


    //ESTADO DE RESULTADOS
    public List<Map<String, Object>> obtenerEstadoResultados(LocalDate desde, LocalDate hasta, int idEmpresa) throws SQLException {
        // Query simplificada que obtiene TODOS los movimientos del periodo
        String sql = """
    SELECT
        c.codigo,
        c.cuenta,
        c.tipocuenta,
        SUM(COALESCE(dp.cargo, 0)) AS total_cargo,
        SUM(COALESCE(dp.abono, 0)) AS total_abono
    FROM tblpartidas p
    INNER JOIN tbldetallepartida dp ON p.idpartida = dp.idpartida
    INNER JOIN tblcatalogocuentas c ON dp.idcuenta = c.idcuenta
    WHERE p.fecha BETWEEN ? AND ?
      AND p.idempresa = ?
      AND c.idempresa = ?
      AND c.tipocuenta IN ('INGRESOS O VENTAS', 'COSTOS', 'GASTOS')
    GROUP BY c.idcuenta, c.codigo, c.cuenta, c.tipocuenta
    ORDER BY
        CASE c.tipocuenta
            WHEN 'INGRESOS O VENTAS' THEN 1
            WHEN 'COSTOS' THEN 2
            WHEN 'GASTOS' THEN 3
        END,
        c.codigo
    """;

        List<Map<String, Object>> resultado = new ArrayList<>();
        double totalIngresos = 0, totalCostos = 0, totalGastos = 0;
        String tipoActual = "";

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Asignar parámetros
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            ps.setInt(3, idEmpresa);
            ps.setInt(4, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipocuenta");
                    String codigo = rs.getString("codigo");
                    String cuenta = rs.getString("cuenta");
                    double totalCargo = rs.getDouble("total_cargo");
                    double totalAbono = rs.getDouble("total_abono");

                    // Calcular saldo según el tipo de cuenta
                    double saldo;
                    if (tipo.equals("INGRESOS O VENTAS")) {
                        // Ingresos: naturaleza acreedora (ABONO - CARGO)
                        saldo = totalAbono - totalCargo;
                    } else {
                        // Costos y Gastos: naturaleza deudora (CARGO - ABONO)
                        saldo = totalCargo - totalAbono;
                    }

                    // Debug: imprimir información
                    System.out.println(String.format("Cuenta: %s - %s | Tipo: %s | Cargo: %.2f | Abono: %.2f | Saldo: %.2f",
                            codigo, cuenta, tipo, totalCargo, totalAbono, saldo));

                    // Solo incluir cuentas con saldo diferente de cero
                    if (Math.abs(saldo) < 0.01) continue;

                    // Agregar encabezado de sección cuando cambia el tipo
                    if (!tipo.equals(tipoActual)) {
                        if (!tipoActual.isEmpty()) {
                            resultado.add(crearFilaVacia());
                        }

                        Map<String, Object> encabezado = new LinkedHashMap<>();
                        encabezado.put("Concepto", ">>> " + tipo);
                        encabezado.put("Monto", null);
                        resultado.add(encabezado);

                        tipoActual = tipo;
                    }

                    // Agregar cuenta
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("Concepto", "  " + codigo + " - " + cuenta);
                    fila.put("Monto", Math.abs(saldo));
                    resultado.add(fila);

                    // Acumular totales
                    if (tipo.equals("INGRESOS O VENTAS")) {
                        totalIngresos += Math.abs(saldo);
                    } else if (tipo.equals("COSTOS")) {
                        totalCostos += Math.abs(saldo);
                    } else if (tipo.equals("GASTOS")) {
                        totalGastos += Math.abs(saldo);
                    }
                }
            }
        }

        // Agregar cálculos finales
        resultado.add(crearFilaVacia());

        // Utilidad Bruta = Ingresos - Costos
        double utilidadBruta = totalIngresos - totalCostos;
        resultado.add(crearFilaTotal("UTILIDAD BRUTA", utilidadBruta));

        // Utilidad Neta = Utilidad Bruta - Gastos
        double utilidadNeta = utilidadBruta - totalGastos;
        resultado.add(crearFilaTotal("UTILIDAD NETA", utilidadNeta));

        // Debug: imprimir totales
        System.out.println("\n=== RESUMEN ===");
        System.out.println("Total Ingresos: $" + totalIngresos);
        System.out.println("Total Costos: $" + totalCostos);
        System.out.println("Total Gastos: $" + totalGastos);
        System.out.println("Utilidad Bruta: $" + utilidadBruta);
        System.out.println("Utilidad Neta: $" + utilidadNeta);
        System.out.println("===============\n");

        return resultado;
    }

    //BALANCE GENERAL
    public List<Map<String, Object>> obtenerBalanceGeneral(LocalDate fechaInicio, LocalDate fechaFin, int idEmpresa) throws SQLException {
        String sql = """
    SELECT 
        c.tipocuenta,
        c.codigo,
        c.cuenta,
        COALESCE(antes.cargo_antes, 0) - COALESCE(antes.abono_antes, 0) + 
        COALESCE(periodo.cargo_periodo, 0) - COALESCE(periodo.abono_periodo, 0) AS saldo_actual
    FROM tblcatalogocuentas c 
    LEFT JOIN (
        SELECT 
            dp.idcuenta,
            SUM(dp.cargo) AS cargo_antes,
            SUM(dp.abono) AS abono_antes
        FROM tbldetallepartida dp
        INNER JOIN tblpartidas p ON dp.idpartida = p.idpartida
        WHERE p.fecha < ? AND p.idempresa = ?
        GROUP BY dp.idcuenta
    ) AS antes ON c.idcuenta = antes.idcuenta
    LEFT JOIN (
        SELECT 
            dp.idcuenta,
            SUM(dp.cargo) AS cargo_periodo,
            SUM(dp.abono) AS abono_periodo
        FROM tbldetallepartida dp
        INNER JOIN tblpartidas p ON dp.idpartida = p.idpartida
        WHERE p.fecha BETWEEN ? AND ? AND p.idempresa = ?
        GROUP BY dp.idcuenta
    ) AS periodo ON c.idcuenta = periodo.idcuenta
    WHERE c.idempresa = ?
    AND c.tipocuenta IN ('ACTIVO CORRIENTE', 'ACTIVO NO CORRIENTE', 'PASIVO CORRIENTE', 'PASIVO NO CORRIENTE', 'CAPITAL')
    ORDER BY 
        CASE c.tipocuenta
            WHEN 'ACTIVO CORRIENTE' THEN 1
            WHEN 'ACTIVO NO CORRIENTE' THEN 2
            WHEN 'PASIVO CORRIENTE' THEN 3
            WHEN 'PASIVO NO CORRIENTE' THEN 4
            WHEN 'CAPITAL' THEN 5
        END,
        c.codigo
    """;

        List<Map<String, Object>> resultado = new ArrayList<>();
        double totalActivo = 0, totalPasivo = 0, totalCapital = 0;
        double subtotalActCorriente = 0, subtotalActNoCorriente = 0;
        double subtotalPasCorriente = 0, subtotalPasNoCorriente = 0;
        String tipoActual = "";

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaInicio));  // Movimientos ANTES del periodo
            ps.setInt(2, idEmpresa);
            ps.setDate(3, Date.valueOf(fechaInicio));  // Movimientos EN el periodo
            ps.setDate(4, Date.valueOf(fechaFin));
            ps.setInt(5, idEmpresa);
            ps.setInt(6, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipocuenta");
                    double saldo = rs.getDouble("saldo_actual");

                    // Agregar subtotal del grupo anterior si cambió
                    if (!tipo.equals(tipoActual) && !tipoActual.isEmpty()) {
                        if (tipoActual.equals("ACTIVO CORRIENTE")) {
                            resultado.add(crearFilaSubtotal("Total Activo Corriente", subtotalActCorriente));
                        } else if (tipoActual.equals("ACTIVO NO CORRIENTE")) {
                            resultado.add(crearFilaSubtotal("Total Activo No Corriente", subtotalActNoCorriente));
                        } else if (tipoActual.equals("PASIVO CORRIENTE")) {
                            resultado.add(crearFilaSubtotal("Total Pasivo Corriente", subtotalPasCorriente));
                        } else if (tipoActual.equals("PASIVO NO CORRIENTE")) {
                            resultado.add(crearFilaSubtotal("Total Pasivo No Corriente", subtotalPasNoCorriente));
                        }
                        resultado.add(crearFilaVacia());
                    }

                    // Agregar encabezado de sección
                    if (!tipo.equals(tipoActual)) {
                        Map<String, Object> encabezado = new LinkedHashMap<>();
                        encabezado.put("Concepto", ">>> " + tipo);
                        encabezado.put("Monto", null);
                        resultado.add(encabezado);
                        tipoActual = tipo;
                    }

                    // Agregar cuenta (solo si tiene saldo diferente de cero)
                    if (Math.abs(saldo) >= 0.01) {
                        Map<String, Object> fila = new LinkedHashMap<>();
                        fila.put("Concepto", "  " + rs.getString("codigo") + " - " + rs.getString("cuenta"));
                        fila.put("Monto", Math.abs(saldo));
                        resultado.add(fila);

                        // Acumular en subtotales y totales
                        if (tipo.equals("ACTIVO CORRIENTE")) {
                            subtotalActCorriente += Math.abs(saldo);
                            totalActivo += Math.abs(saldo);
                        } else if (tipo.equals("ACTIVO NO CORRIENTE")) {
                            subtotalActNoCorriente += Math.abs(saldo);
                            totalActivo += Math.abs(saldo);
                        } else if (tipo.equals("PASIVO CORRIENTE")) {
                            subtotalPasCorriente += Math.abs(saldo);
                            totalPasivo += Math.abs(saldo);
                        } else if (tipo.equals("PASIVO NO CORRIENTE")) {
                            subtotalPasNoCorriente += Math.abs(saldo);
                            totalPasivo += Math.abs(saldo);
                        } else if (tipo.equals("CAPITAL")) {
                            totalCapital += Math.abs(saldo);
                        }
                    }
                }
            }
        }

        // ========== CALCULAR Y AGREGAR UTILIDAD NETA DEL PERIODO ==========
        double utilidadNeta = calcularUtilidadNeta(fechaInicio, fechaFin, idEmpresa);

        // Agregar la utilidad neta al capital
        if (Math.abs(utilidadNeta) >= 0.01) {
            Map<String, Object> filaUtilidad = new LinkedHashMap<>();
            if (utilidadNeta > 0) {
                filaUtilidad.put("Concepto", "  Utilidad Neta del Periodo");
            } else {
                filaUtilidad.put("Concepto", "  Pérdida Neta del Periodo");
            }
            filaUtilidad.put("Monto", Math.abs(utilidadNeta));
            resultado.add(filaUtilidad);

            totalCapital += utilidadNeta;
        }

        // Agregar subtotal de capital
        resultado.add(crearFilaSubtotal("Total Capital", totalCapital));

        // Agregar totales finales
        resultado.add(crearFilaVacia());
        resultado.add(crearFilaTotal("TOTAL ACTIVO", totalActivo));
        resultado.add(crearFilaTotal("TOTAL PASIVO + CAPITAL", totalPasivo + totalCapital));

        return resultado;
    }

    //CALCULAR UTILIDAD NETA
    private double calcularUtilidadNeta(LocalDate desde, LocalDate hasta, int idEmpresa) throws SQLException {
        String sql = """
    SELECT
        SUM(CASE
            WHEN c.tipocuenta = 'INGRESOS O VENTAS'
                THEN COALESCE(dp.abono, 0) - COALESCE(dp.cargo, 0)
            WHEN c.tipocuenta IN ('COSTOS', 'GASTOS')
                THEN COALESCE(dp.cargo, 0) - COALESCE(dp.abono, 0)
            ELSE 0
        END) AS utilidad_neta
    FROM tblpartidas p
    INNER JOIN tbldetallepartida dp ON p.idpartida = dp.idpartida
    INNER JOIN tblcatalogocuentas c ON dp.idcuenta = c.idcuenta
    WHERE p.fecha BETWEEN ? AND ?
      AND p.idempresa = ?
      AND c.idempresa = ?
      AND c.tipocuenta IN ('INGRESOS O VENTAS', 'COSTOS', 'GASTOS')
    """;

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            ps.setInt(3, idEmpresa);
            ps.setInt(4, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double ingresos = 0;
                    double costosGastos = 0;

                    // Recalcular para obtener totales separados
                    String sqlDetallado = """
                SELECT
                    c.tipocuenta,
                    SUM(COALESCE(dp.cargo, 0)) AS total_cargo,
                    SUM(COALESCE(dp.abono, 0)) AS total_abono
                FROM tblpartidas p
                INNER JOIN tbldetallepartida dp ON p.idpartida = dp.idpartida
                INNER JOIN tblcatalogocuentas c ON dp.idcuenta = c.idcuenta
                WHERE p.fecha BETWEEN ? AND ?
                  AND p.idempresa = ?
                  AND c.idempresa = ?
                  AND c.tipocuenta IN ('INGRESOS O VENTAS', 'COSTOS', 'GASTOS')
                GROUP BY c.tipocuenta
                """;

                    try (PreparedStatement ps2 = conn.prepareStatement(sqlDetallado)) {
                        ps2.setDate(1, Date.valueOf(desde));
                        ps2.setDate(2, Date.valueOf(hasta));
                        ps2.setInt(3, idEmpresa);
                        ps2.setInt(4, idEmpresa);

                        try (ResultSet rs2 = ps2.executeQuery()) {
                            while (rs2.next()) {
                                String tipo = rs2.getString("tipocuenta");
                                double cargo = rs2.getDouble("total_cargo");
                                double abono = rs2.getDouble("total_abono");

                                if (tipo.equals("INGRESOS O VENTAS")) {
                                    ingresos += (abono - cargo);
                                } else {
                                    costosGastos += (cargo - abono);
                                }
                            }
                        }
                    }

                    return ingresos - costosGastos;
                }
            }
        }
        return 0;
    }

    //ESTADO DE CAMBIOS EN EL PATRIMONIO
    public List<Map<String, Object>> obtenerEstadoCapital(LocalDate desde, LocalDate hasta, int idEmpresa) throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();

        //CALCULAR SALDO INICIAL DEL CAPITAL (antes del periodo)
        double capitalInicial = calcularCapitalInicial(desde, idEmpresa);

        Map<String, Object> filaInicial = new LinkedHashMap<>();
        filaInicial.put("Concepto", "Capital Inicial");
        filaInicial.put("Monto", Math.abs(capitalInicial));
        resultado.add(filaInicial);

        resultado.add(crearFilaVacia());

        //OBTENER MOVIMIENTOS DEL PERIODO (CAPITAL y RETIROS)
        String sql = """
        SELECT 
            c.tipocuenta,
            SUM(COALESCE(dp.cargo, 0)) AS total_cargo,
            SUM(COALESCE(dp.abono, 0)) AS total_abono
        FROM tblcatalogocuentas c
        INNER JOIN tbldetallepartida dp ON c.idcuenta = dp.idcuenta
        INNER JOIN tblpartidas p ON dp.idpartida = p.idpartida
        WHERE c.idempresa = ?
          AND p.idempresa = ?
          AND c.tipocuenta IN ('CAPITAL', 'RETIROS')
          AND p.fecha BETWEEN ? AND ?
        GROUP BY c.tipocuenta
    """;

        double aportesCapital = 0;
        double retiros = 0;

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);
            ps.setInt(2, idEmpresa);
            ps.setDate(3, Date.valueOf(desde));
            ps.setDate(4, Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipocuenta");
                    double cargo = rs.getDouble("total_cargo");
                    double abono = rs.getDouble("total_abono");

                    if ("CAPITAL".equals(tipo)) {
                        // Capital: Abonos aumentan el capital (aportes)
                        // Cargos disminuyen el capital (retiros registrados en capital)
                        aportesCapital += abono - cargo;
                    } else if ("RETIROS".equals(tipo)) {
                        // Retiros: Cargos son retiros del propietario
                        retiros += cargo - abono;
                    }
                }
            }
        }

        //CALCULAR UTILIDAD NETA DEL PERIODO
        double utilidadNeta = calcularUtilidadNeta(desde, hasta, idEmpresa);

        //AGREGAR MOVIMIENTOS AL RESULTADO
        if (Math.abs(aportesCapital) > 0.01) {
            resultado.add(crearFila("(+) Aportes de Capital", Math.abs(aportesCapital)));
        }

        if (Math.abs(utilidadNeta) > 0.01) {
            if (utilidadNeta > 0) {
                resultado.add(crearFila("(+) Utilidad Neta del Periodo", utilidadNeta));
            } else {
                resultado.add(crearFila("(-) Pérdida Neta del Periodo", Math.abs(utilidadNeta)));
            }
        }

        if (Math.abs(retiros) > 0.01) {
            resultado.add(crearFila("(-) Retiros", retiros));
        }

        //CALCULAR SALDO FINAL
        double capitalFinal = Math.abs(capitalInicial) + aportesCapital + utilidadNeta - retiros;

        resultado.add(crearFilaVacia());
        resultado.add(crearFilaTotal("CAPITAL FINAL", capitalFinal));

        return resultado;
    }

    //CALCULAR CAPITAL INICIAL
    // Calcula el saldo del capital ANTES del periodo especificado.
    private double calcularCapitalInicial(LocalDate fechaInicio, int idEmpresa) throws SQLException {
        String sql = """
        SELECT 
            SUM(COALESCE(dp.cargo, 0)) AS total_cargo,
            SUM(COALESCE(dp.abono, 0)) AS total_abono
        FROM tblcatalogocuentas c
        INNER JOIN tbldetallepartida dp ON c.idcuenta = dp.idcuenta
        INNER JOIN tblpartidas p ON dp.idpartida = p.idpartida
        WHERE c.idempresa = ?
          AND p.idempresa = ?
          AND c.tipocuenta IN ('CAPITAL', 'RETIROS')
          AND p.fecha < ?
    """;

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);
            ps.setInt(2, idEmpresa);
            ps.setDate(3, Date.valueOf(fechaInicio));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double cargo = rs.getDouble("total_cargo");
                    double abono = rs.getDouble("total_abono");

                    // Capital tiene naturaleza acreedora: Abono aumenta, Cargo disminuye
                    double saldo = abono - cargo;

                    return Math.abs(saldo);
                }
            }
        }
        return 0;
    }

    //ESTADO DE FLUJO DE EFECTIVO
    public List<Map<String, Object>> obtenerFlujoEfectivo(LocalDate desde, LocalDate hasta, int idEmpresa) throws SQLException{
        List<Map<String, Object>> resultado = new ArrayList<>();

        // SALDO INICIAL
        double saldoInicial = obtenerSaldoEfectivoFecha(desde.minusDays(1), idEmpresa);
        resultado.add(crearFila("Saldo Inicial de Efectivo", saldoInicial));
        resultado.add(crearFilaVacia());

        // CONSULTA (CORREGIDA)
        String sql = """
        SELECT
            dp_efectivo.cargo AS cargo_caja,
            dp_efectivo.abono AS abono_caja,
            c_otra.tipocuenta AS tipo_relacionado
        FROM tblpartidas p
        INNER JOIN tbldetallepartida dp_efectivo 
            ON p.idpartida = dp_efectivo.idpartida
        INNER JOIN tblcatalogocuentas c_efectivo
            ON dp_efectivo.idcuenta = c_efectivo.idcuenta
        INNER JOIN tbldetallepartida dp_otra
            ON p.idpartida = dp_otra.idpartida
            AND dp_otra.iddetalle != dp_efectivo.iddetalle
        INNER JOIN tblcatalogocuentas c_otra
            ON dp_otra.idcuenta = c_otra.idcuenta
        WHERE p.fecha BETWEEN ? AND ?
        AND p.idempresa = ?
        AND c_efectivo.idempresa = ?
        AND (
            c_efectivo.cuenta ILIKE '%CAJA%' OR
            c_efectivo.cuenta ILIKE '%EFECTIVO%' OR
            c_efectivo.cuenta ILIKE '%BANCO%' OR
            c_efectivo.cuenta ILIKE '%CHEQUE%' OR
            c_efectivo.cuenta ILIKE '%FONDOS%' OR
            c_efectivo.cuenta ILIKE '%DEPOSI%' 
        )
    """;

        Map<String, Double> entradas = new HashMap<>();
        Map<String, Double> salidas = new HashMap<>();

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            ps.setInt(3, idEmpresa);
            ps.setInt(4, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    double cargoCaja = rs.getDouble("cargo_caja");
                    double abonoCaja = rs.getDouble("abono_caja");
                    String tipoRelacionado = rs.getString("tipo_relacionado");

                    if (cargoCaja > 0) {
                        entradas.put(tipoRelacionado, entradas.getOrDefault(tipoRelacionado, 0.0) + cargoCaja);
                    }
                    if (abonoCaja > 0) {
                        salidas.put(tipoRelacionado, salidas.getOrDefault(tipoRelacionado, 0.0) + abonoCaja);
                    }
                }
            }
        }

        // FLUJOS DE OPERACIÓN
        resultado.add(crearFila(">>> FLUJOS DE OPERACIÓN", 0));

        double cobrosVentas = entradas.getOrDefault("INGRESOS O VENTAS", 0.0);
        double cobrosClientes = entradas.getOrDefault("ACTIVO CORRIENTE", 0.0);

        double pagosProveedores = salidas.getOrDefault("PASIVO CORRIENTE", 0.0);
        double pagosGastos = salidas.getOrDefault("GASTOS", 0.0);
        double pagosCostos = salidas.getOrDefault("COSTOS", 0.0);

        resultado.add(crearFila("Cobros a clientes", cobrosVentas + cobrosClientes));
        resultado.add(crearFila("Pagos a proveedores", -(pagosProveedores + pagosCostos)));
        resultado.add(crearFila("Pagos por gastos", -pagosGastos));

        double flujoOperacion = (cobrosVentas + cobrosClientes) - (pagosProveedores + pagosCostos + pagosGastos);
        resultado.add(crearFilaSubtotal("Efectivo neto de operación", flujoOperacion));

        // INVERSIÓN
        resultado.add(crearFilaVacia());
        resultado.add(crearFila(">>> FLUJOS DE INVERSIÓN", 0));

        double pagoActivosNoCorr = salidas.getOrDefault("ACTIVO NO CORRIENTE", 0.0);
        resultado.add(crearFila("Compra de activos fijos", -pagoActivosNoCorr));

        double flujoInversion = -pagoActivosNoCorr;
        resultado.add(crearFilaSubtotal("Efectivo neto de inversión", flujoInversion));

        // FINANCIAMIENTO
        resultado.add(crearFilaVacia());
        resultado.add(crearFila(">>> FLUJOS DE FINANCIAMIENTO", 0));

        double prestamos = entradas.getOrDefault("PASIVO NO CORRIENTE", 0.0);
        double aportesCapital = entradas.getOrDefault("CAPITAL", 0.0);
        double pagosPrestamos = salidas.getOrDefault("PASIVO NO CORRIENTE", 0.0);
        double retiros = salidas.getOrDefault("RETIROS", 0.0);

        resultado.add(crearFila("Préstamos obtenidos", prestamos));
        resultado.add(crearFila("Aportes de capital", aportesCapital));
        resultado.add(crearFila("Pago de préstamos", -pagosPrestamos));

        double flujoFinanciamiento = prestamos + aportesCapital - pagosPrestamos - retiros;
        resultado.add(crearFilaSubtotal("Efectivo neto de financiamiento", flujoFinanciamiento));

        // SALDO FINAL
        resultado.add(crearFilaVacia());

        double aumento = flujoOperacion + flujoInversion + flujoFinanciamiento;
        resultado.add(crearFila("Aumento/Disminución de Efectivo", aumento));

        double saldoFinal = saldoInicial + aumento;
        resultado.add(crearFilaTotal("SALDO FINAL DE EFECTIVO", saldoFinal));

        return resultado;
    }


    private Map<String, Object> crearFilaVacia() {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("Concepto", "");
        fila.put("Monto", null);
        return fila;
    }

    private Map<String, Object> crearFila(String concepto, double monto) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("Concepto", concepto);
        fila.put("Monto", monto);
        return fila;
    }

    private Map<String, Object> crearFilaSubtotal(String concepto, double monto) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("Concepto", "** " + concepto);
        fila.put("Monto", monto);
        return fila;
    }

    private Map<String, Object> crearFilaTotal(String concepto, double monto) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("Concepto", "=== " + concepto);
        fila.put("Monto", monto);
        return fila;
    }

    private double obtenerSaldoEfectivoFecha(LocalDate fecha, int idEmpresa) throws SQLException {
        String sql = """
        SELECT 
            COALESCE(SUM(saldo_cuenta), 0) AS total
        FROM (
            SELECT 
                c.saldo + COALESCE(SUM(dp.cargo), 0) - COALESCE(SUM(dp.abono), 0) AS saldo_cuenta
            FROM tblcatalogocuentas c
            LEFT JOIN tbldetallepartida dp ON c.idcuenta = dp.idcuenta
            LEFT JOIN tblpartidas p ON dp.idpartida = p.idpartida AND p.fecha <= ?
            WHERE c.idempresa = ? 
            AND c.tipocuenta = 'ACTIVO CORRIENTE'
            AND (c.cuenta LIKE '%Caja%' OR c.cuenta LIKE '%Banco%')
            GROUP BY c.idcuenta, c.saldo
        ) AS subconsulta
    """;

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            ps.setInt(2, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    // Registrar reporte generado
    public void registrarReporteGenerado(int idUsuario, String tipoReporte,
                                         LocalDate desde, LocalDate hasta,
                                         String rutaPdf, String observaciones, int idEmpresa) throws SQLException {
        String sql = """
            INSERT INTO tblreportes 
            (idusuario, tipo_reporte, fecha_desde, fecha_hasta, ruta_pdf, observaciones, fecha_generacion, idempresa)
            VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
        """;

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.setString(2, tipoReporte);
            ps.setDate(3, Date.valueOf(desde));
            ps.setDate(4, Date.valueOf(hasta));
            ps.setString(5, rutaPdf);
            ps.setString(6, observaciones);
            ps.setInt(7, idEmpresa);

            ps.executeUpdate();
        }
    }

    public boolean reporteExistente(String tipoReporte, LocalDate desde, LocalDate hasta,int idEmpresa) throws SQLException {
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblreportes " +
                    "WHERE tipo_reporte = ? AND fecha_desde = ? AND fecha_hasta = ? AND idempresa = ?");
            statement.setString(1, tipoReporte);
            statement.setDate(2, Date.valueOf(desde));
            statement.setDate(3, Date.valueOf(hasta));
            statement.setInt(4, idEmpresa);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                return true;
            }

            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int obtenerIdReporte(String tipoReporte, LocalDate desde, LocalDate hasta, int idEmpresa) throws SQLException {
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblreportes " +
                    "WHERE tipo_reporte = ? AND fecha_desde = ? AND fecha_hasta = ? AND idempresa = ?");
            statement.setString(1, tipoReporte);
            statement.setDate(2, Date.valueOf(desde));
            statement.setDate(3, Date.valueOf(hasta));
            statement.setInt(4, idEmpresa);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                return rs.getInt("idreporte");
            }

            return 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void actualizarReporte(int idUsuario, int idReporte, String observaciones, int idEmpresa) throws SQLException {
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("UPDATE tblreportes SET " +
                    "idusuario = ?, observaciones = ?, fecha_generacion = CURRENT_TIMESTAMP, " +
                    "WHERE idempresa = ? AND idreporte = ?");
            statement.setInt(1, idUsuario);
            statement.setString(2, observaciones);
            statement.setInt(3, idEmpresa);
            statement.setInt(4, idReporte);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Obtener información de la empresa
    public Map<String, String> obtenerInfoEmpresa(int idEmpresa) throws SQLException {
        String sql = "SELECT nombre, nit, direccion, telefono FROM tblempresas WHERE idempresa = ?";
        Map<String, String> info = new HashMap<>();

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    info.put("nombre", rs.getString("nombre"));
                    info.put("nit", rs.getString("nit"));
                    info.put("direccion", rs.getString("direccion"));
                    info.put("telefono", rs.getString("telefono"));
                }
            }
        }

        return info;
    }

    public ObservableList<ReporteModel> obtenerReportes(int idEmpresa){
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblreportes WHERE idempresa = ?");
            statement.setInt(1, idEmpresa);

            ResultSet rs = statement.executeQuery();
            ObservableList<ReporteModel> reportes = FXCollections.observableArrayList();
            while (rs.next()) {
                ReporteModel reporte = new ReporteModel();
                reporte.setIdReporte(rs.getInt("idreporteg"));
                reporte.setTipoReporte(rs.getString("tipo_reporte"));
                reporte.setRutaReporte(rs.getString("ruta_pdf"));
                reporte.setFechaDesde(LocalDate.parse(rs.getString("fecha_desde")));
                reporte.setFechaHasta(LocalDate.parse(rs.getString("fecha_hasta")));

                reportes.add(reporte);
            }

            return reportes;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ObservableList<ReporteModel> obtenerReportesPorTipo(int idEmpresa, String tipoReporte){
        try{
            Connection connection = ConexionDB.connection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tblreportes WHERE idempresa = ? AND tipo_reporte = ?");
            statement.setInt(1, idEmpresa);
            statement.setString(2, tipoReporte);

            ResultSet rs = statement.executeQuery();
            ObservableList<ReporteModel> reportes = FXCollections.observableArrayList();
            while (rs.next()) {
                ReporteModel reporte = new ReporteModel();
                reporte.setIdReporte(rs.getInt("idreporteg"));
                reporte.setTipoReporte(rs.getString("tipo_reporte"));
                reporte.setRutaReporte(rs.getString("ruta_pdf"));
                reporte.setFechaDesde(LocalDate.parse(rs.getString("fecha_desde")));
                reporte.setFechaHasta(LocalDate.parse(rs.getString("fecha_hasta")));

                reportes.add(reporte);
            }

            return reportes;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}