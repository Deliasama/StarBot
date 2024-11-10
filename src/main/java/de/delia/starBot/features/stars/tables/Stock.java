package de.delia.starBot.features.stars.tables;

import de.delia.starBot.main.Main;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@NoArgsConstructor
@Table(name = "Stock")
public class Stock {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "members_offer", joinColumns = @JoinColumn(name = "stock_id"))
    @MapKeyColumn(name = "member_id")
    @Column(name = "offer")
    Map<Long, Integer> membersOffer = new HashMap<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private Long guildId;
    @Column
    private int price;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "members_sell", joinColumns = @JoinColumn(name = "stock_id"))
    @Column(name = "member_id")
    private List<Long> membersSell = new ArrayList<>();
    @Column
    private Long offerMemberId;

    @Column
    private int offerValue;

    @Column
    private int phase;

    public Stock(Long guildId, int price, List<Long> membersSell, Map<Long, Integer> membersOffer, Long offerMemberId, int offerValue, int phase) {
        this.guildId = guildId;
        this.price = price;
        this.membersSell = membersSell;
        this.membersOffer = membersOffer;
        this.offerMemberId = offerMemberId;
        this.offerValue = offerValue;
        this.phase = phase;
    }

    public static StockTable getTable() {
        return Main.INSTANCE.stockTable;
    }

    public static class StockTable extends de.delia.starBot.database.Table<Stock> {

        public StockTable() {
            super(Stock.class, Main.INSTANCE.entityManagerFactory);
        }

        public Stock get(long guildId) {
            return getEntityManager(m -> {
                List<Stock> stocks = m.createQuery("SELECT s from Stock s where s.guildId = ?1", Stock.class)
                        .setParameter(1, guildId)
                        .getResultList();
                if (stocks.isEmpty()) {
                    Stock stock = new Stock(guildId, 25, new ArrayList<>(), new HashMap<>(), null, 0, 0);
                    save(stock);
                    return stock;
                }
                return stocks.get(0);
            });
        }

        public Stock update(Stock stock) {
            return getEntityManager(m -> {
                m.getTransaction().begin();
                Stock s = m.find(Stock.class, stock.getId());

                if (s == null) return null;

                s.setPhase(stock.getPhase());
                s.setGuildId(stock.getGuildId());
                s.setPrice(stock.getPrice());
                s.setMembersSell(stock.getMembersSell());
                s.setMembersOffer(stock.getMembersOffer());
                s.setOfferMemberId(stock.getOfferMemberId());
                s.setOfferValue(stock.getOfferValue());

                m.persist(s);
                m.getTransaction().commit();
                return s;
            });
        }
    }
}
