create table reduction_mois (
    id int AUTO_INCREMENT PRIMARY KEY,
    mois_annee varchar(20) NOT NULL,
    reduction_val int NOT NULL,
    signe char(1) NOT NULL
);
create table history(
    id int AUTO_INCREMENT PRIMARY KEY,
    emplouye_name varchar(100) NOT NULL,
    old_salary_slip varchar(100) NOT NULL,
    new_salary_slip varchar(100) NOT NULL,
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

Delete from reduction_mois;
