package mg.erpnext.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import mg.erpnext.model.MonthReduction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

@Service
public class MonthReductionService {
    @Autowired
    private ConnexionService connexionService;

    public void insertMonthReduction(MonthReduction reduction) throws SQLException {
        String sql = "INSERT INTO reduction_mois (mois_annee, reduction_val, signe) VALUES (?, ?, ?)";
        try (Connection conn = connexionService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reduction.getMois_annee());
            stmt.setDouble(2, reduction.getReduction_val());
            stmt.setString(3, String.valueOf(reduction.getSigne()));
            stmt.executeUpdate();
        }
    }

    public List<MonthReduction> getAllMonthReductions() throws SQLException {
        String sql = "SELECT mois_annee, reduction_val, signe FROM reduction_mois";
        List<MonthReduction> reductions = new ArrayList<>();
        try (Connection conn = connexionService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                MonthReduction reduction = new MonthReduction(
                    rs.getString("mois_annee"),
                    rs.getDouble("reduction_val"),
                    rs.getString("signe").charAt(0)
                );
                reductions.add(reduction);
            }
        }
        return reductions;
    }
}
