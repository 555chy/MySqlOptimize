package com.sqlparse.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RowData {
    private List<Object> values;

    public RowData() {
        this.values = new ArrayList<>();
    }

    public RowData(List<Object> values) {
        this.values = new ArrayList<>(values);
    }

    public void addValue(Object value) {
        this.values.add(value);
    }

    public List<Object> getValues() {
        return new ArrayList<>(values);
    }

    public Object getValue(int index) {
        return values.get(index);
    }

    public int size() {
        return values.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RowData rowData = (RowData) o;
        if (values.size() != rowData.values.size()) return false;
        for (int i = 0; i < values.size(); i++) {
            Object val1 = values.get(i);
            Object val2 = rowData.values.get(i);
            if (!Objects.equals(val1, val2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return "RowData" + values.toString();
    }
}
