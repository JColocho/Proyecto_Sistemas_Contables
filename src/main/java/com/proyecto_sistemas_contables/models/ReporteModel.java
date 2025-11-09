package com.proyecto_sistemas_contables.models;

import com.proyecto_sistemas_contables.Conexion.ConexionDB;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReporteModel {
    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // LIBRO DIARIO MEJORADO
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

    // LIBRO MAYOR MEJORADO
    public List<Map<String, Object>> obtenerLibroMayor(LocalDate desde, LocalDate hasta, int idEmpresa) throws SQLException {
        String sql = """
            SELECT 
                c.codigo,
                c.cuenta,
                p.fecha,
                p.concepto,
                CASE 
                    WHEN dp.cargo > 0 THEN dp.cargo 
                    ELSE NULL 
                END AS cargo,
                CASE 
                    WHEN dp.abono > 0 THEN dp.abono 
                    ELSE NULL 
                END AS abono,
                c.saldo AS saldo_inicial
            FROM tblcatalogocuentas c
            INNER JOIN tbldetallepartida dp ON c.idcuenta = dp.idcuenta
            INNER JOIN tblpartidas p ON dp.idpartida = p.idpartida
            WHERE p.fecha BETWEEN ? AND ? 
            AND c.idempresa = ?
            AND p.idempresa = ?
            ORDER BY c.codigo, p.fecha
        """;

        List<Map<String, Object>> resultado = new ArrayList<>();

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            ps.setInt(3, idEmpresa);
            ps.setInt(4, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                String cuentaActual = "";
                double saldoAcumulado = 0;

                while (rs.next()) {
                    String codigo = rs.getString("codigo");
                    String cuenta = rs.getString("cuenta");
                    String codigoCuenta = codigo + " - " + cuenta;

                    // Si cambia la cuenta, agregar fila de encabezado
                    if (!codigoCuenta.equals(cuentaActual)) {
                        if (!cuentaActual.isEmpty()) {
                            // Agregar fila de separación
                            Map<String, Object> separador = new LinkedHashMap<>();
                            separador.put("Cuenta", "");
                            separador.put("Fecha", "");
                            separador.put("Concepto", "");
                            separador.put("Cargo", null);
                            separador.put("Abono", null);
                            separador.put("Saldo", null);
                            resultado.add(separador);
                        }

                        cuentaActual = codigoCuenta;
                        saldoAcumulado = rs.getDouble("saldo_inicial");

                        // Encabezado de cuenta
                        Map<String, Object> encabezado = new LinkedHashMap<>();
                        encabezado.put("Cuenta", ">>> " + codigoCuenta);
                        encabezado.put("Fecha", "");
                        encabezado.put("Concepto", "Saldo Inicial");
                        encabezado.put("Cargo", null);
                        encabezado.put("Abono", null);
                        encabezado.put("Saldo", saldoAcumulado);
                        resultado.add(encabezado);
                    }

                    // Agregar movimiento
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("Cuenta", "");
                    fila.put("Fecha", rs.getDate("fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    fila.put("Concepto", rs.getString("concepto"));

                    Double cargo = rs.getObject("cargo") != null ? rs.getDouble("cargo") : null;
                    Double abono = rs.getObject("abono") != null ? rs.getDouble("abono") : null;

                    fila.put("Cargo", cargo);
                    fila.put("Abono", abono);

                    // Calcular saldo acumulado
                    if (cargo != null) saldoAcumulado += cargo;
                    if (abono != null) saldoAcumulado -= abono;

                    fila.put("Saldo", saldoAcumulado);
                    resultado.add(fila);
                }
            }
        }

        return resultado;
    }

    //ESTADO DE RESULTADOS
    public List<Map<String, Object>> obtenerEstadoResultados(LocalDate desde, LocalDate hasta, int idEmpresa) throws SQLException {
        String sql = """
            SELECT 
                c.tipocuenta,
                c.codigo,
                c.cuenta,
                COALESCE(SUM(dp.cargo), 0) - COALESCE(SUM(dp.abono), 0) AS saldo
            FROM tblcatalogocuentas c
            LEFT JOIN tbldetallepartida dp ON c.idcuenta = dp.idcuenta
            LEFT JOIN tblpartidas p ON dp.idpartida = p.idpartida
                AND p.fecha BETWEEN ? AND ?
            WHERE c.idempresa = ?
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

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            ps.setInt(3, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipocuenta");
                    double saldo = rs.getDouble("saldo");

                    // Agregar encabezado de sección si cambia el tipo
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
                    fila.put("Concepto", "  " + rs.getString("codigo") + " - " + rs.getString("cuenta"));
                    fila.put("Monto", Math.abs(saldo));
                    resultado.add(fila);

                    // Acumular totales
                    if (tipo.equals("INGRESOS O VENTAS")) totalIngresos += Math.abs(saldo);
                    else if (tipo.equals("COSTOS")) totalCostos += Math.abs(saldo);
                    else if (tipo.equals("GASTOS")) totalGastos += Math.abs(saldo);
                }
            }
        }

        // Agregar cálculos finales
        resultado.add(crearFilaVacia());

        double utilidadBruta = totalIngresos - totalCostos;
        resultado.add(crearFilaTotal("UTILIDAD BRUTA", utilidadBruta));

        double utilidadNeta = utilidadBruta - totalGastos;
        resultado.add(crearFilaTotal("UTILIDAD NETA", utilidadNeta));

        return resultado;
    }

    public List<Map<String, Object>> obtenerBalanceGeneral(LocalDate fecha, int idEmpresa) throws SQLException {
        String sql = """
        SELECT 
            c.tipocuenta,
            c.codigo,
            c.cuenta,
            c.saldo + COALESCE(movimientos.total_cargo, 0) - COALESCE(movimientos.total_abono, 0) AS saldo_actual
        FROM tblcatalogocuentas c
        LEFT JOIN (
            SELECT 
                dp.idcuenta,
                SUM(dp.cargo) AS total_cargo,
                SUM(dp.abono) AS total_abono
            FROM tbldetallepartida dp
            INNER JOIN tblpartidas p ON dp.idpartida = p.idpartida
            WHERE p.fecha <= ? AND p.idempresa = ?
            GROUP BY dp.idcuenta
        ) AS movimientos ON c.idcuenta = movimientos.idcuenta
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

            ps.setDate(1, Date.valueOf(fecha));
            ps.setInt(2, idEmpresa);
            ps.setInt(3, idEmpresa);

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
                    if (Math.abs(saldo) >= 0.01) { // Tolerancia para decimales
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

        // Agregar último subtotal
        if (tipoActual.equals("CAPITAL")) {
            resultado.add(crearFilaSubtotal("Total Capital", totalCapital));
        }

        // Agregar totales finales
        resultado.add(crearFilaVacia());
        resultado.add(crearFilaTotal("TOTAL ACTIVO", totalActivo));
        resultado.add(crearFilaTotal("TOTAL PASIVO + CAPITAL", totalPasivo + totalCapital));

        return resultado;
    }

    //ESTADO DE CAMBIOS EN EL PATRIMONIO
    public List<Map<String, Object>> obtenerEstadoCapital(LocalDate desde, LocalDate hasta, int idEmpresa) throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();

        // Obtener saldo inicial del capital
        double saldoInicial = obtenerSaldoCapitalFecha(desde.minusDays(1), idEmpresa);

        Map<String, Object> filaInicial = new LinkedHashMap<>();
        filaInicial.put("Concepto", "Saldo Inicial");
        filaInicial.put("Monto", saldoInicial);
        resultado.add(filaInicial);

        // Obtener movimientos del periodo
        String sql = """
            SELECT 
                p.fecha,
                p.concepto,
                dp.cargo,
                dp.abono
            FROM tblcatalogocuentas c
            INNER JOIN tbldetallepartida dp ON c.idcuenta = dp.idcuenta
            INNER JOIN tblpartidas p ON dp.idpartida = p.idpartida
            WHERE c.idempresa = ?
            AND c.tipocuenta = 'CAPITAL'
            AND p.fecha BETWEEN ? AND ?
            ORDER BY p.fecha
        """;

        double aumentos = 0, disminuciones = 0;

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);
            ps.setDate(2, Date.valueOf(desde));
            ps.setDate(3, Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double cargo = rs.getDouble("cargo");
                    double abono = rs.getDouble("abono");

                    if (cargo > 0) {
                        aumentos += cargo;
                    }
                    if (abono > 0) {
                        disminuciones += abono;
                    }
                }
            }
        }

        // Agregar movimientos
        resultado.add(crearFilaVacia());
        resultado.add(crearFila("(+) Aumentos de Capital", aumentos));
        resultado.add(crearFila("(-) Disminuciones de Capital", disminuciones));

        // Calcular saldo final
        double saldoFinal = saldoInicial + aumentos - disminuciones;
        resultado.add(crearFilaVacia());
        resultado.add(crearFilaTotal("SALDO FINAL", saldoFinal));

        return resultado;
    }

    //ESTADO DE FLUJO DE EFECTIVO
    public List<Map<String, Object>> obtenerFlujoEfectivo(LocalDate desde, LocalDate hasta, int idEmpresa) throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();

        //SALDO INICIAL
        double saldoInicial = obtenerSaldoEfectivoFecha(desde.minusDays(1), idEmpresa);

        Map<String, Object> filaInicial = new LinkedHashMap<>();
        filaInicial.put("Concepto", "Saldo Inicial de Efectivo");
        filaInicial.put("Monto", saldoInicial);
        resultado.add(filaInicial);
        resultado.add(crearFilaVacia());

        //OBTENER TODOS LOS MOVIMIENTOS DE EFECTIVO
        String sqlMovimientos = """
        SELECT 
            c_otra.tipocuenta AS tipo_relacionado,
            SUM(dp_efectivo.cargo) AS total_cargos,
            SUM(dp_efectivo.abono) AS total_abonos
        FROM tblpartidas p
        INNER JOIN tbldetallepartida dp_efectivo ON p.idpartida = dp_efectivo.idpartida
        INNER JOIN tblcatalogocuentas c_efectivo ON dp_efectivo.idcuenta = c_efectivo.idcuenta
        INNER JOIN tbldetallepartida dp_otra ON p.idpartida = dp_otra.idpartida 
            AND dp_otra.iddetalle != dp_efectivo.iddetalle
        INNER JOIN tblcatalogocuentas c_otra ON dp_otra.idcuenta = c_otra.idcuenta
        WHERE p.fecha BETWEEN ? AND ?
        AND p.idempresa = ?
        AND c_efectivo.idempresa = ?
        AND c_efectivo.tipocuenta = 'ACTIVO CORRIENTE'
        GROUP BY c_otra.tipocuenta
    """;

        Map<String, Double> entradas = new HashMap<>();
        Map<String, Double> salidas = new HashMap<>();

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sqlMovimientos)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            ps.setInt(3, idEmpresa);
            ps.setInt(4, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipoRelacionado = rs.getString("tipo_relacionado");
                    double cargos = rs.getDouble("total_cargos");
                    double abonos = rs.getDouble("total_abonos");

                    entradas.put(tipoRelacionado, entradas.getOrDefault(tipoRelacionado, 0.0) + cargos);
                    salidas.put(tipoRelacionado, salidas.getOrDefault(tipoRelacionado, 0.0) + abonos);
                }
            }
        }

        //FLUJOS DE OPERACIÓN
        double cobrosVentas = entradas.getOrDefault("INGRESOS O VENTAS", 0.0);
        double cobrosClientes = entradas.getOrDefault("ACTIVO CORRIENTE", 0.0);
        double pagosProveedores = salidas.getOrDefault("PASIVO CORRIENTE", 0.0);
        double pagosGastos = salidas.getOrDefault("GASTOS", 0.0);
        double pagosCostos = salidas.getOrDefault("COSTOS", 0.0);

        Map<String, Object> encabOperacion = new LinkedHashMap<>();
        encabOperacion.put("Concepto", ">>> FLUJOS DE OPERACIÓN");
        encabOperacion.put("Monto", null);
        resultado.add(encabOperacion);

        resultado.add(crearFila("Cobros a clientes", cobrosVentas + cobrosClientes));
        resultado.add(crearFila("Pagos a proveedores", -(pagosProveedores + pagosCostos)));
        resultado.add(crearFila("Pagos por gastos", -pagosGastos));

        double flujoOperacion = (cobrosVentas + cobrosClientes) - (pagosProveedores + pagosCostos + pagosGastos);
        resultado.add(crearFilaSubtotal("Efectivo neto de operación", flujoOperacion));

        //FLUJOS DE INVERSIÓN
        resultado.add(crearFilaVacia());

        double compraActivosCorrientes = entradas.getOrDefault("ACTIVO NO CORRIENTE", 0.0);
        double pagoActivosNoCorrientes = salidas.getOrDefault("ACTIVO NO CORRIENTE", 0.0);

        Map<String, Object> encabInversion = new LinkedHashMap<>();
        encabInversion.put("Concepto", ">>> FLUJOS DE INVERSIÓN");
        encabInversion.put("Monto", null);
        resultado.add(encabInversion);

        resultado.add(crearFila("Compra de activos fijos", -pagoActivosNoCorrientes));

        double flujoInversion = -pagoActivosNoCorrientes;
        resultado.add(crearFilaSubtotal("Efectivo neto de inversión", flujoInversion));

        // ==================== FLUJOS DE FINANCIAMIENTO ====================
        resultado.add(crearFilaVacia());

        double prestamosRecibidos = entradas.getOrDefault("PASIVO NO CORRIENTE", 0.0);
        double aportesCapital = entradas.getOrDefault("CAPITAL", 0.0);
        double pagosPrestamos = salidas.getOrDefault("PASIVO NO CORRIENTE", 0.0);
        double retiroCapital = salidas.getOrDefault("CAPITAL", 0.0);

        Map<String, Object> encabFinanciamiento = new LinkedHashMap<>();
        encabFinanciamiento.put("Concepto", ">>> FLUJOS DE FINANCIAMIENTO");
        encabFinanciamiento.put("Monto", null);
        resultado.add(encabFinanciamiento);

        resultado.add(crearFila("Préstamos obtenidos", prestamosRecibidos));
        resultado.add(crearFila("Aportes de capital", aportesCapital));
        resultado.add(crearFila("Pago de préstamos", -pagosPrestamos));

        double flujoFinanciamiento = prestamosRecibidos + aportesCapital - pagosPrestamos - retiroCapital;
        resultado.add(crearFilaSubtotal("Efectivo neto de financiamiento", flujoFinanciamiento));

        // ==================== SALDO FINAL ====================
        resultado.add(crearFilaVacia());
        double aumentoEfectivo = flujoOperacion + flujoInversion + flujoFinanciamiento;
        resultado.add(crearFila("Aumento/Disminución de efectivo", aumentoEfectivo));

        double saldoFinal = saldoInicial + aumentoEfectivo;
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

    private double obtenerSaldoCapitalFecha(LocalDate fecha, int idEmpresa) throws SQLException {
        String sql = """
        SELECT 
            COALESCE(SUM(saldo_cuenta), 0) AS total
        FROM (
            SELECT 
                c.saldo + COALESCE(SUM(dp.cargo), 0) - COALESCE(SUM(dp.abono), 0) AS saldo_cuenta
            FROM tblcatalogocuentas c
            LEFT JOIN tbldetallepartida dp ON c.idcuenta = dp.idcuenta
            LEFT JOIN tblpartidas p ON dp.idpartida = p.idpartida AND p.fecha <= ?
            WHERE c.idempresa = ? AND c.tipocuenta = 'CAPITAL'
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

    private double obtenerFlujoPorTipo(LocalDate desde, LocalDate hasta, int idEmpresa,
                                       String tipoCuenta, String filtroNombre, boolean esCargo) throws SQLException {
        String columna = esCargo ? "dp.cargo" : "dp.abono";
        String filtroAdicional = (filtroNombre != null && !filtroNombre.trim().isEmpty())
                ? "AND c.cuenta LIKE ? "
                : "";

        String sql = String.format("""
        SELECT COALESCE(SUM(%s), 0) AS total
        FROM tblcatalogocuentas c
        INNER JOIN tbldetallepartida dp ON c.idcuenta = dp.idcuenta
        INNER JOIN tblpartidas p ON dp.idpartida = p.idpartida
        WHERE c.idempresa = ? 
        AND c.tipocuenta = ?
        AND p.fecha BETWEEN ? AND ?
        %s
        """, columna, filtroAdicional);

        try (Connection conn = ConexionDB.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);
            ps.setString(2, tipoCuenta);
            ps.setDate(3, Date.valueOf(desde));
            ps.setDate(4, Date.valueOf(hasta));

            if (filtroNombre != null && !filtroNombre.trim().isEmpty()) {
                ps.setString(5, "%" + filtroNombre + "%");
            }

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
}