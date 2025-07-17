package mg.erpnext.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mg.erpnext.model.History;

@Service
public class HistoryService {
    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);
    
    @Autowired
    private ConnexionService connexionService;
     
    public void insertMonthReduction(History reduction) throws SQLException {
        String sql = "INSERT INTO history (emplouye_name, old_salary_slip, new_salary_slip, updated_at) VALUES (?, ?, ?, ?)";
        
        logger.info("Tentative d'insertion dans l'historique pour employé: {}", reduction.getEmplouye_name());
        logger.debug("Détails de l'insertion - ancienne fiche: {}, nouvelle fiche: {}, date: {}", 
            reduction.getOld_salary_slip(), reduction.getNew_salary_slip(), reduction.getUpdated_at());
        
        try (Connection conn = connexionService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, reduction.getEmplouye_name());
            stmt.setDouble(2, reduction.getOld_salary_slip());
            stmt.setDouble(3, reduction.getNew_salary_slip());
            stmt.setObject(4, reduction.getUpdated_at());
            
            int rowsAffected = stmt.executeUpdate();
            logger.info("Insertion réussie. Lignes affectées: {}", rowsAffected);
            
        } catch (SQLException e) {
            logger.error("Échec de l'insertion dans l'historique", e);
            throw e;
        }
    }
    
    public List<History> getAllHistorys() {
        String sql = "SELECT * FROM history";
        List<History> histories = new ArrayList<>();
        logger.info("Récupération de tous les historiques");
        
        try (Connection conn = connexionService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                History history = new History(
                    rs.getInt("id"),
                    rs.getString("emplouye_name"),
                    Double.parseDouble(rs.getString("old_salary_slip")),
                    Double.parseDouble(rs.getString("new_salary_slip")),
                    rs.getObject("updated_at", LocalDateTime.class)
                );
                histories.add(history);
            }
            logger.info("Nombre d'historiques récupérés: {}", histories.size());
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des historiques", e);
        }
        return histories;
    }
}