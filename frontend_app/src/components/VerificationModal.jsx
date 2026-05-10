import React, {useState} from "react";

const VerificationModal = ({ onClose, onSend, categories }) => {
    const [message, setMessage] = useState('');
    const [selectedCategories, setSelectedCategories] = useState([]);

    const toggleCategory = (catName) => {
        setSelectedCategories(prev =>
            prev.includes(catName) ? prev.filter(c => c !== catName) : [...prev, catName]
        );
    };

    return (
        <div className="modal-overlay" style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000
        }}>
            <div className="modal-content" style={{
                background: 'white', padding: '2rem', borderRadius: '8px', width: '90%', maxWidth: '500px'
            }}>
                <h3>Request Shop Verification</h3>
                <p style={{ color: '#555', marginBottom: '1rem' }}>
                    Select the categories you want to sell and leave a message for the administrators.
                </p>

                <div style={{ marginBottom: '1rem', maxHeight: '150px', overflowY: 'auto', border: '1px solid #ddd', padding: '0.5rem', borderRadius: '4px' }}>
                    {categories.map(cat => (
                        <label key={cat.id} style={{ display: 'block', marginBottom: '0.5rem', cursor: 'pointer' }}>
                            <input
                                type="checkbox"
                                value={cat.name || cat.title}
                                checked={selectedCategories.includes(cat.name || cat.title)}
                                onChange={() => toggleCategory(cat.name || cat.title)}
                                style={{ marginRight: '0.5rem' }}
                            />
                            {cat.name || cat.title}
                        </label>
                    ))}
                </div>

                <textarea
                    placeholder="Why should we verify your shop? (Optional)"
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    style={{ width: '100%', minHeight: '100px', padding: '0.5rem', marginBottom: '1rem', borderRadius: '4px', border: '1px solid #ddd' }}
                />

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
                    <button onClick={onClose} style={{ padding: '8px 16px', background: '#404040', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Cancel</button>
                    <button onClick={() => onSend(selectedCategories, message)} style={{ padding: '8px 16px', background: '#d35400', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Send Request</button>
                </div>
            </div>
        </div>
    );
};

export default VerificationModal;