CREATE TABLE UsageService (
                              hour TIMESTAMP PRIMARY KEY,
                              community_produced DECIMAL(10, 2) NOT NULL DEFAULT 0,
                              community_used DECIMAL(10, 2) NOT NULL DEFAULT 0,
                              grid_used DECIMAL(10, 2) NOT NULL DEFAULT 0
);

CREATE TABLE CurrentPercentageService (
                                          hour TIMESTAMP PRIMARY KEY,
                                          community_depleted DECIMAL(10, 2) NOT NULL DEFAULT 0,
                                          grid_portion DECIMAL(10, 2) NOT NULL DEFAULT 0
);