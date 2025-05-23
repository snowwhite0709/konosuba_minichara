package com.example.konosuba_chara.model;

import java.util.ArrayList;
import java.util.List;

public class MiniCharaRecordFile {
    private List<MiniCharaRecord> records;

    public MiniCharaRecordFile() {
        records = new ArrayList<>();
    }

    public void addRecord(MiniCharaRecord record) {
        records.add(record);
    }

    public List<MiniCharaRecord> getRecords() {
        return records;
    }
}
