CREATE TABLE trade
  (
     product VARCHAR(255),
     portfolio VARCHAR(255),
     book VARCHAR(255),
     tradeId NUMBER PRIMARY KEY,
     submitterId NUMBER,
     submitterDealId NUMBER,
     dealType VARCHAR(255),
     bidType VARCHAR(255),
     currentValue NUMBER,
     previousValue NUMBER,
     pl1 NUMBER,
     pl2 NUMBER,
     gainDx NUMBER,
     sxPx NUMBER,
     x99Out NUMBER,
     batch NUMBER
  ) with "template=partitioned";

CREATE INDEX idx_t1_product ON trade(product);

CREATE INDEX idx_t1_portfolio ON trade(portfolio);

CREATE INDEX idx_t1_book ON trade(book);