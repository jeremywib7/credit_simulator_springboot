package com.credit.constant;

public final class ErrorMessage {

    public static final String FIELD_VEHICLE_TYPE = "Jenis kendaraan";
    public static final String FIELD_CONDITION = "Kondisi kendaraan";
    public static final String FIELD_YEAR = "Tahun kendaraan";
    public static final String FIELD_LOAN = "Jumlah pinjaman";
    public static final String FIELD_TENOR = "Tenor";
    public static final String FIELD_DOWN_PAYMENT = "Jumlah DP";

    public static final String REQUIRED = "%s wajib diisi.";
    public static final String ALPHABET_ONLY = "%s hanya boleh huruf.";
    public static final String NUMBER_ONLY = "%s harus berupa angka.";

    public static final String VEHICLE_TYPE_INVALID = "Jenis kendaraan harus Mobil atau Motor.";
    public static final String CONDITION_INVALID = "Kondisi kendaraan harus Baru atau Bekas.";
    public static final String YEAR_FORMAT = "Tahun kendaraan harus 4 digit angka.";
    public static final String YEAR_IN_FUTURE = "Tahun kendaraan tidak boleh lebih dari %d.";
    public static final String YEAR_NEW_TOO_OLD = "Tahun kendaraan baru tidak boleh kurang dari %d.";
    public static final String LOAN_NOT_POSITIVE = "Jumlah pinjaman harus lebih dari 0.";
    public static final String LOAN_TOO_BIG = "Jumlah pinjaman maksimal 1 miliar.";
    public static final String TENOR_FORMAT =
            "Tenor harus berupa angka " + LoanRules.MIN_TENOR + "-" + LoanRules.MAX_TENOR + " tahun.";
    public static final String TENOR_RANGE =
            "Tenor harus " + LoanRules.MIN_TENOR + "-" + LoanRules.MAX_TENOR + " tahun.";
    public static final String DP_NEGATIVE = "Jumlah DP tidak boleh negatif.";
    public static final String DP_NOT_BELOW_LOAN = "Jumlah DP harus lebih kecil dari jumlah pinjaman.";
    public static final String DP_BELOW_MINIMUM = "DP kendaraan %s minimal %s%% dari jumlah pinjaman.";

    public static final String FILE_UNREADABLE = "Gagal membaca file '%s': %s";
    public static final String FILE_INCOMPLETE =
            "File harus berisi " + LoanRules.INPUT_FILE_LINES + " baris: jenis, kondisi, tahun, pinjaman, tenor, DP.";
    public static final String UNKNOWN_COMMAND = "Command '%s' tidak dikenal. Ketik 'show'.";
    public static final String NOTHING_TO_SAVE =
            "Belum ada hasil hitung untuk disimpan. Jalankan 'input' atau 'load'.";
    public static final String SHEET_NAME_REQUIRED = "Nama sheet wajib diisi.";

    public static final String API_INTERRUPTED = "Pemanggilan API terputus.";
    public static final String API_CALL_FAILED = "Gagal memanggil API: %s";
    public static final String API_HTTP_STATUS = "API membalas HTTP %d.";
    public static final String API_INVALID_JSON = "Respons API bukan JSON valid.";
    public static final String API_NOT_AN_OBJECT = "Respons API kosong atau bukan objek JSON.";
    public static final String API_FIELD_MISSING = "Field '%s' tidak ada pada respons API.";

    private ErrorMessage() {
    }
}
